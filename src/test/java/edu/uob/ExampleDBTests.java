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
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE" + randomName + ";");
        // no space after DATABASE
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
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALER TABLE marks ADD age;");
        // first word wrong
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    @Test
    public void testInvalidQuery29() {
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
    public void testInvalidQuery30() {
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
    public void testInvalidQuery31() {
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
    public void testInvalidQuery32() {
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
    public void testInvalidQuery33() {
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
    public void testInvalidQuery34() {
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
    public void testInvalidQuery35() {
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
    public void testInvalidQuery36() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (value, height);");
        String response = sendCommandToServer("ALTER TABLE marks AD age;");
        // alteration type not ADD/DROP
        assertTrue(response.contains("[ERROR]"), "An [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An [OK] tag was returned");
    }

    // tested invalid query entries up until ALTER
    // should start again at INSERT INTO

}
