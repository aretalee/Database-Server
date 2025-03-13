package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
        "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that suitable IDs are being generated and returned !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Simon';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Simon';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Simon"), "Simon was added to a table and the server restarted - but Simon was not returned by SELECT *");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }

    // Tests the Use query
    @Test
    public void testUse() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String randomNameTwo = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomNameTwo + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("USE " + randomNameTwo + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Rob"), "An attempt was made to add Rob to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
        sendCommandToServer("USE " + randomName + ";");
        String responseTwo = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(responseTwo.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(responseTwo.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(responseTwo.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(responseTwo.contains("Sion"), "An attempt was made to add Sion to the table, but they were not returned by SELECT *");
        assertFalse(responseTwo.contains("Rob"), "There should not be a name called Rob in the table");
    }

    // Tests the Update query
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testUpdate() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("UPDATE marks SET name = 'Neil' WHERE mark == 20;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Neil"), "An attempt was made to update Chris to Neil to the table, but they were not returned by SELECT *");
        assertFalse(response.contains("Chris"), "There should not be a name called Chris in the table");
    }

    // Tests the Alter query (add)
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testAlterAdd() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("ALTER TABLE marks ADD class;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("class"), "An attempt was made to add Class header to table, but it was not returned by SELECT *");
    }

    @Test
    public void testAlterDrop() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("ALTER TABLE marks DROP name;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("name"), "There should not be a name column in the table");
        assertFalse(response.contains("Chris"), "There should not be a name called Chris in the table");
    }

    @Test
    public void testDelete() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("DELETE FROM marks WHERE mark<60;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertFalse(response.contains("55"), "There should not be a mark = 55 in the table");
        assertFalse(response.contains("Sion"), "There should not be a name called Sion in the table");
    }

    @Test
    public void testDrop() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("DROP TABLE marks;");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(response.contains("[OK]"), "An invalid query was made, however an [OK] tag was returned");
        assertTrue(response.contains("[ERROR]"), "An invalid query was made, however an [ERROR] tag was not returned");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
        String responseTwo = sendCommandToServer("USE " + randomName + ";");
        assertFalse(responseTwo.contains("[OK]"), "An invalid query was made, however an [OK] tag was returned");
        assertTrue(responseTwo.contains("[ERROR]"), "An invalid query was made, however an [ERROR] tag was not returned");
    }

    @Test
    public void testJoin() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE cause (number, name);");
        sendCommandToServer("INSERT INTO cause VALUES (1.10, 'Dan');");
        sendCommandToServer("INSERT INTO cause VALUES (1.12, 'Ben');");
        sendCommandToServer("CREATE TABLE result (title, mark);");
        sendCommandToServer("INSERT INTO result VALUES ('Fantasy', 1.12);");
        sendCommandToServer("INSERT INTO result VALUES ('Mystery', 1.10);");
        String response = sendCommandToServer("JOIN cause AND result ON number AND mark;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("cause.name"), "There should be a cause.name header in the table");
        assertTrue(response.contains("result.title"), "There should should be a result.title header in the table");
        assertTrue(response.contains("Dan"), "There should be a name called Dan in the table");
        assertTrue(response.contains("Fantasy"), "There should be a title called Fantasy in the table");
    }

    @Test
    public void testIDUpdate() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("DELETE FROM marks WHERE mark<21;");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("5"), "There should be an id = 5 in the table");
        assertFalse(response.contains("4"), "There should not be an id = 4 in the table, as corresponding row has been deleted");
    }



    // below tests check if errors in query structure or malformed inputs are caught

    @Test
    public void testInvalidQuery1() {
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        // calling any other command before USE or CREATE DATABASE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    //USE

    @Test
    public void testInvalidQuery2() {
        String response = sendCommandToServer("USE data;");
        // calling USE on database that doesn't exist
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery3() {
        String response = sendCommandToServer("USE data");
        // not enough tokens for USE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery4() {
        String response = sendCommandToServer("USE dat*a;");
        // invalid database name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery5() {
        String response = sendCommandToServer("US data;");
        // first word wrong
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery6() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("USEdat*a;");
        // no space after USE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // CREATE DATABASE

    @Test
    public void testInvalidQuery7() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREAE DATABASE " + randomName + ";");
        // wrong first word
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery8() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("USE" + randomName + ";");
        // no space after USE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery9() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATEDATABASE " + randomName + ";");
        // no space after CREATE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery10() {
        String response = sendCommandToServer("CREATE DATABASE;");
        // not enough values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery11() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE" + randomName);
        // missing semicolon
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery12() {
        String response = sendCommandToServer("CREATE DATABASE ma%k ;");
        // invalid database name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery13() {
        String response = sendCommandToServer("CREATE DATAASE ma%k ;");
        // 2nd word not database
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // CREATE TABLE

    @Test
    public void testInvalidQuery14() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATETABLE marks;");
        // no space before table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery15() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TALE marks;");
        // 2nd word not table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery16() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLEmarks;");
        // no space before table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery17() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks");
        // missing semicolon
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery18() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE mar(ks;");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery19() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (value;");
        // missing closing bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery20() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks value);");
        // missing opening bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery21() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (val&%ue);");
        // invalid attribute name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery22() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (value height);");
        // missing comma between attributes
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // DROP DATABASE

    @Test
    public void testInvalidQuery23() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DROPDATABASE " + randomName + ";");
        // no space after drop
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery24() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DROP DATABASE" + randomName + ";");
        // no space after database
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery25() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DRP DATABASE " + randomName + ";");
        // first word wrong
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery26() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DR0P DAABASE " + randomName + ";");
        // 2nd word not database or table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // DROP TABLE

    @Test
    public void testInvalidQuery27() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DR0P TABLE ma898*;");
        // invalid database/table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery28() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DR0P TABLE ;");
        // not enough values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // ALTER

    @Test
    public void testInvalidQuery29() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALER TABLE marks ADD age;");
        // first word wrong
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery30() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER BEET marks ADD age;");
        // 2nd word not table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery31() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTERTABLE marks ADD age;");
        // no space after alter
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery32() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLEmarks ADD age;");
        // no space after TABLE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery33() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE marksADD age;");
        // no space before alteration type
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery34() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE marks ADDage;");
        // no space after alteration type
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery36() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE mark(s ADD age;");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery37() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE marks ADD ag)e;");
        // invalid attribute name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery38() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE marks AD age;");
        // alteration type not ADD/DROP
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery39() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE mark age;");
        // not enough values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // INSERT

    @Test
    public void testInvalidQuery40() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSRT INTO marks VALUES (1, 1);");
        // first word wrong
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery41() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT ITO marks VALUES (1, 1);");
        // 2nd word not INTO
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery42() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO ma*rks VALUES (1, 1);");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery43() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VAUES (1, 1);");
        // 4th word not values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery44() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES 1, 1);");
        // missing opening bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery45() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (1, 1;");
        // missing closing bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery46() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (1 1);");
        // missing comma between values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery47() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (that, 1);");
        // invalid string literal value
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery48() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (1+, 1);");
        // incorrect placement of + in int
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery49() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (.1, 1);");
        // incorrect placement of . in int
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery50() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marks VALUES (1., 1);");
        // incorrect placement of . in int
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // SELECT

    @Test
    public void testInvalidQuery51() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SEECT * FROM marks;");
        // wrong first word
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery52() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT  FROM marks;");
        // missing header/ * from query
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery53() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FOM marks;");
        // 3rd token not FROM
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery54() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM **md);");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery55() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT va^lue FROM marks;");
        // invalid attribute name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery56() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT value height FROM marks;");
        // missing comma in attribute list
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery57() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHER value == 1;");
        // token after table name is not WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // WHERE

    @Test
    public void testInvalidQuery58() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value => 1;");
        // incorrect operator format
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery59() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value >< 1;");
        // incorrect operator format
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery60() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value ! 1;");
        // not a valid operator
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery61() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE va(lue != 1;");
        // invalid attribute name in condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery62() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value != tru;");
        // invalid value in condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery63() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value 1;");
        // missing operator in simple condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery64() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks == 1;");
        // missing attribute name in simple condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery65() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value ==;");
        // missing value in simple condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery66() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value;");
        // missing multiple values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery67() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE;");
        // has WHERE but missing condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery68() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 adn height == 170;");
        // boolean operator is not and/or
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery69() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and heiht == 170;");
        // wrong attribute name v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery70() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and height -= 170;");
        // wrong operator v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery71() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and height == .170;");
        // wrong value v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery72() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and height == 1 and value;");
        // missing a parameter v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery73() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and height == 1 and value;");
        // missing multiple values v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery74() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and  == 1 and == 1;");
        // missing multiple values v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery75() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE (value == 1 and height == 1;");
        // missing closing bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery76() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and height == 1);");
        // missing opening bracket
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery77() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1and height == 1;");
        // missing space before and
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery78() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 andheight == 1;");
        // missing space before and
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery79() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE ((value==1 and height == 1) or value > 1;");
        // missing bracket v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery80() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE value == 1 and ;");
        // missing 2nd condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery81() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHERE and value == 1;");
        // missing 2nd condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // UPDATE

    @Test
    public void testInvalidQuery82() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDTE marks SET value = 38 WHERE VALUE == 1;");
        // wrong first word
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery83() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE ma(ks SET value = 38 WHERE VALUE == 1;");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery84() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks ST value = 38 WHERE VALUE == 1;");
        // 3rd token not SET
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery85() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET va)lue = 38 WHERE VALUE == 1;");
        // invalid attribute name in NameValueList
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery86() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value == 38 WHERE VALUE == 1;");
        // invalid comparator in NameValueList
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery87() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = nul WHERE VALUE == 1;");
        // invalid value in NameValueList
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery88() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = 38 height = 180 WHERE VALUE == 1;");
        // missing comma in NameValueList
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery89() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = 38, heigh_t = 180 WHERE VALUE == 1;");
        // invalid attribute name v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery90() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = 38, height = 180 WERE VALUE == 1;");
        // token after NameValueList not WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // extra test for SELECT
    @Test
    public void testInvalidQuery91() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT value, height FOM marks;");
        // missing comma in attribute list
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // extra tests for missing spaces

    @Test
    public void testInvalidQuery92() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERTINTO marks VALUES (0.1,1);");
        // missing space before INTO
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery93() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTOmarks VALUES (0.1,1);");
        // missing space after INTO
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery94() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("INSERT INTO marksVALUES (0.1,1);");
        // missing space before VALUE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery95() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT* FROM marks WHERE value == 1 and height == 1;");
        // missing space after SELECT
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery96() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT *FROM marks WHERE value == 1 and height == 1;");
        // missing space before FROM
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery97() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROMmarks WHERE value == 1 and height == 1;");
        // missing space after FROM
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery98() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marksWHERE value == 1 and height == 1;");
        // missing space before WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery99() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks WHEREvalue == 1 and height == 1;");
        // missing space after WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // continuing with UPDATE

    @Test
    public void testInvalidQuery100() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATEmarks SET value = 38, height = 180 WHERE VALUE == 1;");
        // missing space after UPDATE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery101() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marksSET value = 38, height = 180 WHERE VALUE == 1;");
        // missing space before SET
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery102() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SETvalue = 38, height = 180 WHERE VALUE == 1;");
        // missing space after SET
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery103() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = 38, height = 180WHERE VALUE == 1;");
        // missing space before WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery104() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("UPDATE marks SET value = 38, height = 180 WHEREVALUE == 1;");
        // missing space after WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // DELETE

    @Test
    public void testInvalidQuery105() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELTE FROM marks WHERE VALUE == 1;");
        // wrong first word
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery106() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FRM marks WHERE VALUE == 1;");
        // 2nd token not FROM
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery107() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FROM mar)ks WHERE VALUE == 1;");
        // invalid table name
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery108() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FROM marks WHRE VALUE == 1;");
        // 4th token not WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery109() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETEFROM marks WHERE VALUE == 1;");
        // missing space after DELETE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery110() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FROMmarks WHERE VALUE == 1;");
        // missing space after FROM
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery111() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FROM marksWHERE VALUE == 1;");
        // missing space before WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery112() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("DELETE FROM marks WHEREVALUE == 1;");
        // missing space after WHERE
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // JOIN
    @Test
    public void testInvalidQuery113() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JIN orders AND drinks ON order AND type;");
        // wrong first word
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery114() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN or&ders AND drinks ON order AND type;");
        // invalid table one
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery115() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AD drinks ON order AND type;");
        // 3rd token not AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery116() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drin*ks ON order AND type;");
        // invalid table two
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery117() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks N order AND type;");
        // 5th token not ON
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery118() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ON ord-er AND type;");
        // invalid attribute name one
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery119() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ON order AD type;");
        // 7th token not AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery120() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ON order AND t!pe;");
        // invalid attribute name 2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery121() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOINorders AND drinks ON order AND type;");
        // missing space after JOIN
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery122() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN ordersAND drinks ON order AND type;");
        // missing space before 1st AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery123() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders ANDdrinks ON order AND type;");
        // missing space after 1st AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery124() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinksON order AND type;");
        // missing space before ON
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery125() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ONorder AND type;");
        // missing space after ON
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery126() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ON orderAND type;");
        // missing space before 2nd AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery127() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE drinks (type, amount);");
        sendCommandToServer("INSERT INTO drinks VALUES ('tea', 1);");
        sendCommandToServer("INSERT INTO drinks VALUES ('coffee', 2);");
        sendCommandToServer("CREATE TABLE orders (name, order);");
        sendCommandToServer("INSERT INTO orders VALUES ('Sam', coffee');");
        sendCommandToServer("INSERT INTO orders VALUES ('Janet', 'tea');");
        String response = sendCommandToServer("JOIN orders AND drinks ON order ANDtype;");
        // missing space after 2nd AND
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery128() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT *, FROM marks;");
        // invalid attribute
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery129() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks where value == \"Simon\";");
        // string literals must be enclosed in single not double quotes
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery130() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks where va.lue == 10;");
        // more invalid attribute names
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery131() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks where value == 10+;");
        // more invalid numbers
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery132() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        sendCommandToServer("INSERT INTO marks VALUES (0.1,1);");
        String response = sendCommandToServer("SElECT * FROM marks where value == .10;");
        // even more invalid numbers
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // should test for too few/many values in all query types as well?

    // end of BNF testing

    // now testing queries that conform to BNF but are attempting prohibited actions

    // USE
    @Test
    public void invalidActions1() {
        String response = sendCommandToServer("USE results;");
        // no databases exist yet
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // CREATE

    @Test
    public void invalidActions2() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        // must call use before table-specific commands
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions3() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("DROP TABLE marks (name, mark, pass);");
        // must call use before table-specific commands v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions4() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("USE results;");
        // selected database does not exist
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions5() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        // database already exists
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions6() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("CREATE TABLE marks (name);");
        // table already exists
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions7() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (name, id);");
        // can't add ID as column header
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions8() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (name, NamE);");
        // can't add duplicate header
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions9() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (name, NamE);");
        // can't add duplicate header
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // DROP

    @Test
    public void invalidActions10() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("DROP DATABASE there;");
        // can't drop database that doesn't exist
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions11() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("DROP TABLE mark;");
        // can't drop table that doesn't exist
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions12() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("DROP TABLE mark;");
        // can't drop table that doesn't exist
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // ALTER

    @Test
    public void invalidActions13() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE marks add id;");
        // can't add id as header using ALTER
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions14() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE marks add name;");
        // can't add duplicate columns
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions15() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE mark add pass;");
        // can't use ALTER ADD on non-existent table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions16() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE marks drop pass;");
        // can't drop non-existent column
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions17() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE mark DROP pass;");
        // can't use ALTER DROP on non-existent table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions18() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("ALTER TABLE marks drop id;");
        // can't drop id column
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // INSERT
    @Test
    public void invalidActions19() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("INSERT INTO mark VALUES ('Bob');");
        // can't use INSERT on non-existent table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions20() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name);");
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 20);");
        // trying to insert too many values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions21() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark);");
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 20, TRUE);");
        // trying to insert too few values
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions22() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks;");
        String response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 20, TRUE);");
        // trying to insert into table that doesn't have headers
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // SELECT

    @Test
    public void invalidActions23() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM mark;");
        // can't SELECT from non-existing table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions24() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT name, age FROM marks;");
        // can't SELECT non-existing column
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // WHERE

    @Test
    public void invalidActions25() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE and age == 1;");
        // can't query non-existent column in condition
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // UPDATE
    @Test
    public void invalidActions26() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("UPDATE mark SET pass = FALSE WHERE pass == TRUE;");
        // can't UPDATE non-existent table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions27() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("UPDATE marks SET age = 29 WHERE pass == TRUE;");
        // can't UPDATE non-existent column
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions28() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("UPDATE marks SET ID = 29 WHERE pass == TRUE;");
        // can't UPDATE id
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // DELETE

    @Test
    public void invalidActions29() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("DELETE FROM mark WHERE pass == TRUE;");
        // can't DELETE from non-existent table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // JOIN

    @Test
    public void invalidActions30() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN mark AND coursework ON mark AND result;");
        // can't JOIN non-existent tables
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions31() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN marks AND courrk ON mark AND result;");
        // can't JOIN non-existent tables v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions32() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN marks AND marks ON mark AND pass;");
        // can't JOIN the same table
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions33() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN marks AND coursework ON result AND mark;");
        // one or more attributes doesn't belong to their respective tables
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions34() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN marks AND coursework ON mark AND pass;");
        // one or more attributes doesn't belong to their respective tables v2
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions35() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (title, result);");
        sendCommandToServer("INSERT INTO marks VALUES ('OXO', 20);");
        sendCommandToServer("INSERT INTO marks VALUES ('DB', 80);");
        String response = sendCommandToServer("JOIN marks AND coursework ON title AND result;");
        // one or more attributes doesn't belong to their respective tables v3
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions36() {
        String response = sendCommandToServer("CREATE DATABASE null;");
        // can't create table with reserved keyword
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions37() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE from (name, mark, pass);");
        // can't create table with reserved keyword
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions38() {
        String response = sendCommandToServer("CREATE DATABASE drop;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions39() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE use (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions40() {
        String response = sendCommandToServer("CREATE DATABASE alter;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions41() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE create (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions42() {
        String response = sendCommandToServer("CREATE DATABASE insert;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions43() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE select (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions44() {
        String response = sendCommandToServer("CREATE DATABASE delete;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions45() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE update (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions46() {
        String response = sendCommandToServer("CREATE DATABASE join;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions47() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE and (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions48() {
        String response = sendCommandToServer("CREATE DATABASE on;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions49() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE where (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions50() {
        String response = sendCommandToServer("CREATE DATABASE table;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions51() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE database (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions52() {
        String response = sendCommandToServer("CREATE DATABASE into;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions53() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE values (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions54() {
        String response = sendCommandToServer("CREATE DATABASE set;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions55() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE add (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions56() {
        String response = sendCommandToServer("CREATE DATABASE true;");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions57() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE false (name, mark, pass);");
        // more reserved keyword tests
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions58() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE false == TRUE and mark == 1;");
        // reserved keyword in other parts of query test
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void invalidActions59() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        String response = sendCommandToServer("ALTER TABLE marks DROP select;");
        // reserved keyword in other parts of query test
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // general

    @Test
    public void invalidActions70() {
        String response = sendCommandToServer("");
        // reminds the user that they haven't entered any commands
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // end of prohibited actions testing

    // now testing queries that should work/ return [OK]

    @Test
    public void allowedActions1() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Simon';");
        // == comparison on strings should be case-sensitive
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "Not returned by SELECT *");
    }

    @Test
    public void allowedActions2() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name == 'simon';");
        // == comparison on strings should be case-sensitive v2
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertFalse(response.contains("Simon"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions3() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name like 'S';");
        // LIKE should be case-sensitive
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "Not returned by SELECT *");
    }

    @Test
    public void allowedActions4() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 's';");
        // LIKE should be case-sensitive v2
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertFalse(response.contains("Simon"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions5() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE mark LIKE '5';");
        // LIKE on non-strings should be treated as string comp
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("65"), "Not returned by SELECT *");
        assertTrue(response.contains("55"), "Not returned by SELECT *");
    }

    @Test
    public void allowedActions6() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE mark LIKE '1';");
        // if no rows that match conditions, return just column headers
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("name"), "Not returned by SELECT *");
        assertTrue(response.contains("mark"), "Not returned by SELECT *");
        assertTrue(response.contains("pass"), "Not returned by SELECT *");
        assertFalse(response.contains("Simon"), "Was returned by SELECT *");
        assertFalse(response.contains("Sion"), "Was returned by SELECT *");
        assertFalse(response.contains("Bob"), "Was returned by SELECT *");
        assertFalse(response.contains("FALSE"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions7() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        // allowed to delete databases that contain tables
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
    }

    @Test
    public void allowedActions8() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("DROP TABLE marks;");
        // allowed to delete tables that contain data
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
    }

    @Test
    public void allowedActions9() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE mark LIKE '1';");
        // allowed to delete databases that contain tables
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("name"), "Not returned by SELECT *");
        assertTrue(response.contains("mark"), "Not returned by SELECT *");
        assertTrue(response.contains("pass"), "Not returned by SELECT *");
        assertFalse(response.contains("Simon"), "Was returned by SELECT *");
        assertFalse(response.contains("Sion"), "Was returned by SELECT *");
        assertFalse(response.contains("Bob"), "Was returned by SELECT *");
        assertFalse(response.contains("FALSE"), "Was returned by SELECT *");
    }

    // whitespace (too much and lack of)
    // test other methods as well?

    @Test
    public void allowedActions10() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE mark>20 and mark<80;");
        // no whitespace in condition value pairs is allowed
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "Not returned by SELECT *");
        assertTrue(response.contains("Sion"), "Not returned by SELECT *");
        assertFalse(response.contains("Bob"), "Was returned by SELECT *");
        assertFalse(response.contains("Chris"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions11() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 80, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT name,pass FROM marks WHERE name LIKE 's';");
        // no whitespace in attribute list is allowed
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Chris"), "Not returned by SELECT *");
        assertTrue(response.contains("FALSE"), "Not returned by SELECT *");
    }

    @Test
    public void allowedActions12() {
        // varying whitespace in between keywords
        String randomName = generateRandomName();
        String response = sendCommandToServer("   CREATE      DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer(" USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("CREATE     TABLE     marks      (name   , mark, pass  );");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response =  sendCommandToServer("INSERT INTO marks VALUES (     'Simon',65,TRUE    );");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT  INTO  marks    VALUES ('Sion', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT  INTO marks   VALUES ('Bob', 80, TRUE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT INTO marks VALUES   ('Chris', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("SELECT name,pass FROM marks WHERE name      ==    'Chris';");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Chris"), "Not returned by SELECT *");
        assertTrue(response.contains("FALSE"), "Not returned by SELECT *");
    }


    // case-insensitive stuff (table/database/attribute names + SQL keywords)
    // test other methods as well?
    @Test
    public void allowedActions13() {
        // varying case for keywords + names (should be case-insensitive)
        String randomName = generateRandomName();
        String response = sendCommandToServer("   create      dAtaBase " + randomName + ";");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer(" UsE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("create table marks     (name   , mark, pass  );");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response =  sendCommandToServer("insert into MARKS VALUES (     'Simon',65,TRUE    );");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT  INTO  marks    VALUES ('Sion', 55, TRUE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT  INTO marks   VALUES ('Bob', 80, TRUE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("INSERT INTO marks values   ('Chris', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        response = sendCommandToServer("SELECT NAME,paSs FROM marks WHERE naMe      ==    'Chris';");
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Chris"), "Not returned by SELECT *");
        assertTrue(response.contains("FALSE"), "Not returned by SELECT *");
    }

    // columns saved with case, values also case sensitive

    @Test
    public void allowedActions14() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (Name, currentMark, PASS);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        // columns should retain case in the file
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Name"), "Not returned by SELECT *");
        assertTrue(response.contains("currentMark"), "Not returned by SELECT *");
        assertTrue(response.contains("PASS"), "Not returned by SELECT *");
        assertFalse(response.contains("name"), "Was returned by SELECT *");
        assertFalse(response.contains("CURRENTMARK"), "Was returned by SELECT *");
        assertFalse(response.contains("pass"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions15() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks values ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        // values should retain case in the file
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("Chris"), "Not returned by SELECT *");
        assertTrue(response.contains("FALSE"), "Not returned by SELECT *");
        assertFalse(response.contains("chris"), "Was returned by SELECT *");
        assertFalse(response.contains("CHRIS"), "Was returned by SELECT *");
        assertFalse(response.contains("false"), "Was returned by SELECT *");
        assertFalse(response.contains("False"), "Was returned by SELECT *");
    }

    // make sure floats e.g. 1.10 and 1.1 don't match

    @Test
    public void allowedActions16() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (number);");
        sendCommandToServer("INSERT INTO marks values (1.10);");
        sendCommandToServer("INSERT INTO marks values (1.1);");
        String response = sendCommandToServer("SELECT * FROM marks where number == 1.1;");
        // floats must match exactly in comparison to be returned
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("1.1"), "Not returned by SELECT *");
        assertFalse(response.contains("1.10"), "Was returned by SELECT *");
    }

    // make sure comparisons work with +/- and decimals

    @Test
    public void allowedActions17() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (number);");
        sendCommandToServer("INSERT INTO marks values (+2);");
        sendCommandToServer("INSERT INTO marks values (5);");
        String response = sendCommandToServer("SELECT * FROM marks where number < +5;");
        // + ignored when doing comparisions
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("2"), "Not returned by SELECT *");
        assertFalse(response.contains("5"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions18() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (number);");
        sendCommandToServer("INSERT INTO marks values (-6);");
        sendCommandToServer("INSERT INTO marks values (-12);");
        String response = sendCommandToServer("SELECT * FROM marks where number <= 1;");
        // - taken into account for comparisons
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("-6"), "Not returned by SELECT *");
        assertTrue(response.contains("-12"), "Not returned by SELECT *");
    }

    @Test
    public void allowedActions19() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (number);");
        sendCommandToServer("INSERT INTO marks values (1.10);");
        sendCommandToServer("INSERT INTO marks values (1.11);");
        String response = sendCommandToServer("SELECT * FROM marks where number >= 1.0;");
        // floats comparisons
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertTrue(response.contains("1.11"), "Not returned by SELECT *");
        assertTrue(response.contains("1.10"), "Not returned by SELECT *");
    }

    // make sure comparisons are right, including between different types (blank result)

    @Test
    public void allowedActions20() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (number);");
        sendCommandToServer("INSERT INTO marks values (1.10);");
        sendCommandToServer("INSERT INTO marks values (1.11);");
        String response = sendCommandToServer("SELECT * FROM marks where number == 'that';");
        // comparison between different types should yield blank result (e.g. int vs string)
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertFalse(response.contains("1.11"), "Was returned by SELECT *");
        assertFalse(response.contains("1.10"), "Was returned by SELECT *");
    }

    @Test
    public void allowedActions21() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value);");
        sendCommandToServer("INSERT INTO marks values ('this');");
        sendCommandToServer("INSERT INTO marks values ('that');");
        String response = sendCommandToServer("SELECT * FROM marks where value == TRUE ;");
        // comparison between different types should yield blank result (e.g. string vs bool)
        assertTrue(response.contains("[OK]"), "An [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "An [ERROR] tag was returned");
        assertFalse(response.contains("this"), "Was returned by SELECT *");
        assertFalse(response.contains("that"), "Was returned by SELECT *");
    }


    // compound queries

    // more complicated joins (many to many)

    // maybe double check list parsing again (Value, Attribute, Wild Attribute, Name Value)?


    // test even more or all methods for whitespace and case (now only testing some)


}
