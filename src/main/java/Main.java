import flute.config.Config;
import flute.utils.file_processing.FileProcessor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import me.tongfei.progressbar.ProgressBar;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private final static Logger logger = Logger.getLogger("extract-inherit-elements");
        private static ArrayList<Record> readCsv(String filePath) {
            CsvRW reader = new CsvRW();
            return reader.read(Path.of(filePath));
        }

    private static ASTParser newParser(String projectName, String projectDir) {
        Config.autoConfigure(projectName, projectDir);
        ASTParser parser = ASTParser.newParser(Config.JDT_LEVEL);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        Hashtable<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(Config.JAVA_VERSION, options);
        parser.setCompilerOptions(options);
        parser.setEnvironment(Config.CLASS_PATH, Config.SOURCE_PATH, Config.ENCODE_SOURCE, true);
        return parser;
    }
    private static String parseFile(String projectName, String projectDir, String filePath) {
        ASTParser parser = newParser(projectName, projectDir);
        parser.setUnitName(filePath);
        parser.setSource(FileProcessor.read(new File(filePath)).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        int numClass = cu.types().size();
        String[] filePathParts = filePath.split("/");
        String fileName = filePathParts[filePathParts.length - 1];
        String className = fileName.replace(".java", "");
        TypeDeclaration targetClass = null;
        if (numClass > 0) {
            if (numClass >= 2) {
                logger.log(Level.INFO, "The file " + filePath + " has more than one class");
            }
            try {
                targetClass = (TypeDeclaration) cu.types().get(0);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "The file " + filePath + "has no class");
                return "<no_class>";
            }
            String superClass = targetClass.resolveBinding().getSuperclass().getQualifiedName();
            if (superClass.isEmpty() || superClass.equals("java.lang.Object")) {
//                logger.log(Level.INFO, "The class in file " + filePath + " has no super class");
                return "<no_super_class>";
            } else {
                String[] methods = Arrays.stream(targetClass.resolveBinding().getSuperclass().getDeclaredMethods()).map(Object::toString).filter(str -> str.contains("public")).toArray(String[]::new);
                // need to filter methods to only get public methods
                String[] vars = Arrays.stream(targetClass.resolveBinding().getSuperclass().getDeclaredFields()).map(Object::toString).toArray(String[]::new);
                return "<methods>" + String.join(",", methods) + "<variables>" + String.join(",", vars);
            }
        } else {
            logger.log(Level.SEVERE, "The file " + filePath + " has no class");
            return "<no_class>";
        }
    }


    public static void main(String[] args) {
        Config.JAVAFX_DIR = "/home/lvdthieu/Token";
        String csvFile = args[0];
        String baseDir = args[1];
        ArrayList<Record> dataset = readCsv("/home/lvdthieu/iluwatar.csv");
        System.out.println("Read file done");
        List<String> inherit_elements = new ArrayList<>(dataset.size());
        for (Record record : ProgressBar.wrap(dataset, "Extracting")) {
            String projName = record.proj_name;
            String projDir = baseDir + "/" + projName;
            String filePath = projDir + "/" + record.relative_path;
            inherit_elements.add(parseFile(projName, projDir, filePath));
        }
        int countNoClass = 0;
        int countNoSuperClass = 0;
        for (String inherit_element : inherit_elements) {
            if (inherit_element.contains("<no_class>")) {
                countNoClass++;
            }
            if (inherit_element.contains("<no_super_class>")) {
                countNoSuperClass++;
            }
        }
        System.out.println("No class: " + countNoClass);
        System.out.println("No super class: " + countNoSuperClass);
//        for (int i = 0; i < dataset.size(); i++) {
//            dataset.get(i).setInherit_elements(inherit_elements.get(i));
//        }

    }
}

