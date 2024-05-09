import flute.config.Config;
import flute.utils.file_processing.FileProcessor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

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
    private final static Logger logger = Logger.getLogger("java-parser");
    private static ArrayList<Record> readCsv(String filePath, String task) {
        CsvRW reader = new CsvRW();
        return reader.read(Path.of(filePath), task);
    }
    private static void storeCsv(String filePath, ArrayList<Record> dataset, String task) {
        CsvRW writer = new CsvRW();
        writer.write(Path.of(filePath), dataset, task);
    }

    private static ASTParser newParser(String projectName, String projectDir) {
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

    private static String getInheritElements(String projectName, String projectDir, String filePath) {
        ASTParser parser = newParser(projectName, projectDir);
        parser.setUnitName(filePath);
        parser.setSource(FileProcessor.read(new File(filePath)).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        int numClass = cu.types().size();
        TypeDeclaration targetClass;
        if (numClass > 0) {
            if (numClass >= 2) {
                logger.log(Level.INFO, "The file has more than one class");
            }
            try {
                targetClass = (TypeDeclaration) cu.types().get(0);
            } catch (Exception e) {
                logger.log(Level.INFO, "The file has enum first");
                return "<enum>";
            }
            ITypeBinding binding = targetClass.resolveBinding();
            if (binding == null) {
                logger.log(Level.INFO, "The class cannot be resolved binding");
                return "<binding>";
            }
            ITypeBinding superClass = binding.getSuperclass();
            if (superClass == null) {
                logger.log(Level.INFO, "The class is java.lang.Object class");
                return "<object>";
            }
            String superClassName = superClass.getQualifiedName();
            if (superClassName.isEmpty() || superClassName.equals("java.lang.Object") || superClassName.equals("null")) {
                return "<no_super_class>";
            } else {
                String[] methods = Arrays.stream(targetClass.resolveBinding().getSuperclass().getDeclaredMethods()).map(Object::toString).filter(str -> str.contains("public")).toArray(String[]::new);
                String[] vars = Arrays.stream(targetClass.resolveBinding().getSuperclass().getDeclaredFields()).map(Object::toString).toArray(String[]::new);
                return "<methods>" + String.join(",", methods) + "<variables>" + String.join(",", vars);
            }
        } else {
            logger.log(Level.INFO, "The file has no class");
            return "<no_class>";
        }
    }

    private static String getMethodQualifiedName(String projectName, String projectDir, String filePath, String className, String methodName) {
        ASTParser parser;
        try {
             parser = newParser(projectName, projectDir);
        } catch (Exception e) {
            logger.info("Can not configure project");
            return "<no_parser>";
        }
        parser.setUnitName(filePath);
        parser.setSource(FileProcessor.read(new File(filePath)).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        int numClass = cu.types().size();
        TypeDeclaration targetClass = null;
        if (numClass > 0) {
            if (numClass >= 2) {
                logger.log(Level.INFO, "The file has more than one class");
            }
            for (int i = 0; i < numClass; i++) {
                if ((((TypeDeclaration) cu.types().get(i)).resolveBinding() != null) &&
                        ((((TypeDeclaration) cu.types().get(i)).resolveBinding().getName().equals(className)))) {
                    targetClass = (TypeDeclaration) cu.types().get(i);
                    break;
                }
            }
            if (targetClass != null) {
                for (IMethodBinding method : targetClass.resolveBinding().getDeclaredMethods()) {
                    if ((method.getName() != null) && (method.getName().equals(methodName))) {
                        return method.toString();
                    }
                }
            }
            return "<method_not_found>";
        } else {
            logger.log(Level.INFO, "The file has no class");
            return "<no_class>";
        }
    }

    public static void main(String[] args) {
        Config.JAVAFX_DIR = "/home/lvdthieu/Token";
        String inputFile = args[0];
        String baseDir = args[1];
        String outputFile = args[2];
        String task = args[3];
        ArrayList<Record> dataset = readCsv(inputFile, task);
        System.out.println("Read file done");

        if (task.equals("<inherit_elements>")) {
            List<String> inherit_elements = new ArrayList<>(dataset.size());
            for (Record record : ProgressBar.wrap(dataset, "Extracting")) {
                String projName = record.proj_name;
                String projDir = baseDir + "/" + projName;
                String filePath = projDir + "/" + record.relative_path;
                inherit_elements.add(getInheritElements(projName, projDir, filePath));
            }
            for (int i = 0; i < dataset.size(); i++) {
                dataset.get(i).setInheritElement(inherit_elements.get(i));
            }
        }
        else if (task.equals("<method_qualified_names>")) {
            List<String> method_qualified_names = new ArrayList<>(dataset.size());
            for (Record record : ProgressBar.wrap(dataset, "Extracting")) {
                String projName = record.proj_name;
                String projDir = baseDir + "/" + projName;
                String filePath = projDir + "/" + record.relative_path;
                String className = record.class_name;
                String methodName = record.func_name;
                method_qualified_names.add(getMethodQualifiedName(projName, projDir, filePath, className, methodName));
            }
            for (int i = 0; i < dataset.size(); i++) {
                dataset.get(i).setMethodQualifiedName(method_qualified_names.get(i));
            }
        }
        storeCsv(outputFile, dataset, task);
//        int countNoClass = 0;
//        int countNoSuperClass = 0;
//        for (String inherit_element : inherit_elements) {
//            if (inherit_element.contains("<no_class>")) {
//                countNoClass++;
//            }
//            if (inherit_element.contains("<no_super_class>")) {
//                countNoSuperClass++;
//            }
//        }
//        System.out.println("No class: " + countNoClass);
//        System.out.println("No super class: " + countNoSuperClass);
    }
}

