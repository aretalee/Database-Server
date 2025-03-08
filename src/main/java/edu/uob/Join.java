package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Join {

    // remember to check if table exists + if attributes exist
//    if (chosenTable == null) {
//        server.setErrorLine("Requested table does not exist.");
//        return false;
//    } else if (!table.accessColumnHeaders().contains(headerName) || !table.accessColumnHeaders().contains(headerNameTwo)) {
//        server.setErrorLine("Requested column(s) in SET does not exist.");
//        return false;
//    }


    // check which rows to join
    // ensure ordering of tables is same as that of attributes
    // also need to check if attribute 1 is in table 1 & attribute 2 is in table 2

    public void joinTables(Table tableOne, Table tableTwo) {

        // create new TABLE object for temp storage (if like SQL only need to generate output and no need so save?)
        // make headers first (need to append OG table name)
        List<String> headerList = new ArrayList<>();
        for (String header : tableOne.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id")) {
                headerList.add(tableOne.getTableName() + "." + header);
            }
        }
        for (String header : tableTwo.accessColumnHeaders()) {
            if (!header.equalsIgnoreCase("id")) {
                headerList.add(tableTwo.getTableName() + "." + header);
            }
        }

        Table jointTable = new Table(null, headerList, "none");


        // maybe loop through foreign key row in table 2
        // then check table one to see if there's a match
        // if found a match -> add table 1 content first then add table two content behind

        //

        // call SELECT to print them out

    }



}
