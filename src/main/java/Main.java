import flute.config.Config;
import flute.utils.file_processing.FileProcessor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;


public class Main {
    private static String parseFile(String projectName, String projectDir, String filePath, String className) {
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
        parser.setUnitName(filePath);
        parser.setSource(FileProcessor.read(new File(filePath)).toCharArray());
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        int numClass = cu.types().size();
        TypeDeclaration targetClass = null;
        try {
            if (numClass > 0) {
                for (int i = 0; i < numClass; i++) {
                    TypeDeclaration type = (TypeDeclaration) cu.types().get(i);
                    if (numClass == 1 || type.getName().toString().equals(className)) {
                        targetClass = type;
                        break;
                    }
                }
                if (targetClass == null) {
                    targetClass = (TypeDeclaration) cu.types().get(0);
                }
            } else {
                return "<no_class>";
            }
        } catch (Exception e) {
            return "<enum>";
        }
        ITypeBinding binding = targetClass.resolveBinding();
        if (binding == null) {
            return "<binding>";
        }
        ITypeBinding superClass = binding.getSuperclass();
        if (superClass == null) {
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
    }

    public static void main(String[] args) {
        Config.JAVAFX_DIR = "/home/lvdthieu/Token";
        String baseDir = args[0];
        String projName = args[1];
        String relativePath = args[2];
        String className = args[3];
        String projDir = baseDir + "/" + projName;
        String filePath = projDir + "/" + relativePath;
        try {
            System.out.println(parseFile(projName, projDir, filePath, className));
        } catch (Exception e) {
            System.out.println("<encounter_error>");
        }
    }
}

