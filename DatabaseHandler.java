import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;

public class DatabaseHandler {
    private Connection dbConnection;

    public DatabaseHandler(String dbFilePath) throws SQLException {
        dbConnection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
    }

    public void executeSQLCommand(String sqlCommand) throws SQLException {
        Statement stmt = dbConnection.createStatement();

        if (sqlCommand.trim().toUpperCase().startsWith("SELECT")) {
            try (ResultSet rs = stmt.executeQuery(sqlCommand)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Print column headers
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + "\t");
                }
                System.out.println();
                System.out.println("----------------------------------------------------");
                
                // Print rows
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    for (int i = 1; i <= columnCount; i++) {
                        System.out.print(rs.getString(i) + "\t");
                    }
                    System.out.println();
                }
                
                // Print total row count
                System.out.println("Total rows: " + rowCount);
            }
        } else {
            // Handle other SQL commands (INSERT, UPDATE, DELETE, etc.)
            stmt.execute(sqlCommand);
            System.out.println("Executed command: " + sqlCommand);
        }

        stmt.close();
    }


    public byte[] readDatabaseFile(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] fileContent = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileContent);
        }
        return fileContent;
    }

    public void close() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }
}