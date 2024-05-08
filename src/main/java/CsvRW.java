import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvRW {
    private static final Logger logger = Logger.getLogger("csv");
    public ArrayList<Record> read(Path path, String task) {
        ArrayList<Record> list = new ArrayList<>();
        try {
            // Prepare list.
            int initialCapacity = (int) Files.lines(path).count();
            list = new ArrayList<>( initialCapacity );

            // Read CSV file
            BufferedReader reader = Files.newBufferedReader(path);
            Iterable < CSVRecord > records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(reader);
            for ( CSVRecord record : records )
            {
                String proj_name = record.get("proj_name");
                String relative_path = record.get("relative_path");
                String class_name = record.get("class_name");
                String func_name = record.get("func_name");
                String masked_class = record.get("masked_class");
                String func_body = record.get("func_body");
                int len_input = Integer.parseInt(record.get("len_input"));
                int len_output = Integer.parseInt(record.get("len_output"));
                int total = Integer.parseInt(record.get("total"));
                if (task.equals("<inherit_elements>")) {
                    Record aRecord = new Record(proj_name, relative_path, class_name, func_name, masked_class, func_body, len_input, len_output, total);
                    list.add(aRecord);
                } else if (task.equals("<method_qualified_names>")) {
                    String inherit_element = record.get("inherit_elements");
                    Record aRecord = new Record(proj_name, relative_path, class_name, func_name, masked_class, func_body, len_input, len_input, total, inherit_element);
                    list.add(aRecord);
                }

            }
        } catch (IOException e)
        {
            logger.log(Level.SEVERE, e.getMessage());
        }
        return list;
    }

    public void write(final Path path, ArrayList<Record> dataset, String task) {
        try (final CSVPrinter printer = CSVFormat.RFC4180.withHeader("proj_name", "relative_path", "class_name", "func_name", "masked_class", "func_body", "len_input", "len_output", "total", "inherit_elements").print(path, StandardCharsets.UTF_8))
        {
            for (Record aRecord : dataset) {
                printer.printRecord(aRecord.proj_name,
                        aRecord.relative_path,
                        aRecord.class_name,
                        aRecord.func_name,
                        aRecord.masked_class,
                        aRecord.func_body,
                        aRecord.len_input,
                        aRecord.len_output,
                        aRecord.total,
                        aRecord.inherit_element
                );
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Encounter error while writing csv file");
        }
    }
}
