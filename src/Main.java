
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author Phil Adriaan
 */
public class Main extends Application {

    private static final String KEYWORD = "\"We are notifying you because there are ";
    private static final int MESSAGE_COLUMN_NUMBER = 0;
    private static final int COUNT_COLUMN_NUMBER = 1;
    private static final String OUTPUT_FILE_NAME = "Aggregated OMS Dump " + System.currentTimeMillis() + ".xlsx";
    private static final String MESSAGE_HEADER = "Message";
    private static final String COUNT_HEADER = "Count";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            File dump_file = new FileChooser().showOpenDialog(stage);
            if (dump_file != null) {
                BufferedReader buffered_reader = new BufferedReader(new FileReader(dump_file));
                String line = buffered_reader.readLine();
                Map<String, Integer> aggregation = new TreeMap();
                while (line != null) {
                    line.trim();
                    if (line.contains(KEYWORD)) {
                        line = line.substring(KEYWORD.length(), line.length());
                        int count = Integer.valueOf(line.substring(0, line.indexOf(" ")));
                        String message = line.substring(line.indexOf("\"\"") + 2, line.lastIndexOf("\"\""));

                        if (aggregation.containsKey(message)) {
                            aggregation.put(message, aggregation.get(message) + count);
                        } else {
                            aggregation.put(message, count);
                        }
                    }
                    line = buffered_reader.readLine();
                }
                buffered_reader.close();
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("OMS Dump");
                Row header = sheet.createRow(0);
                header.createCell(MESSAGE_COLUMN_NUMBER).setCellValue(MESSAGE_HEADER);
                header.createCell(COUNT_COLUMN_NUMBER).setCellValue(COUNT_HEADER);
                int row_number = 1;
                for (String i : aggregation.keySet()) {
                    Row row = sheet.createRow(row_number);
                    row.createCell(MESSAGE_COLUMN_NUMBER).setCellValue(i);
                    row.createCell(COUNT_COLUMN_NUMBER).setCellValue(aggregation.get(i));
                    row_number++;
                }
                sheet.autoSizeColumn(MESSAGE_COLUMN_NUMBER);
                sheet.autoSizeColumn(COUNT_COLUMN_NUMBER);
                FileOutputStream fileOut = new FileOutputStream(OUTPUT_FILE_NAME);
                workbook.write(fileOut);
                fileOut.close();
            }
        } catch (Exception exception) {
            new Alert(Alert.AlertType.ERROR, exception.getMessage()).showAndWait();
        }
        Platform.exit();
    }
}
