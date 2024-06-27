import flute.config.Config;
import flute.utils.file_processing.FileProcessor;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;

public class Main {

    private static CompilationUnit createCU(String projectName, String projectDir, String filePath) {
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
        return cu;
    }

    public static void main(String[] args) {
        Config.JAVAFX_DIR = "/home/lvdthieu/Token";
        String baseDir = args[0];
        String projectName = args[1];
        String relativePath = args[2];
        String className = args[3];
        String projectDir = baseDir + "/" + projectName;
        String filePath = projectDir + "/" + relativePath;
        try {
            CompilationUnit cu = createCU(projectName, projectDir, filePath);
            int numClass = cu.types().size();
            if (numClass == 0) {
                System.out.println("<no_class>");
                return;
            }
            AbstractTypeDeclaration targetClass = null;
            for (int i = 0; i < numClass; i++) {
                AbstractTypeDeclaration type = (AbstractTypeDeclaration) cu.types().get(i);
                System.out.println(type.getName().toString());
                if (type.getName().toString().equals(className)) {
                    targetClass = type;
                }
            }
            if (targetClass == null) {
                System.out.println("<cant_find_class>");
                return;
            }
            if (targetClass instanceof TypeDeclaration) {
                ITypeBinding binding = ((TypeDeclaration) targetClass).resolveBinding();
                if (binding == null) {
                    System.out.println("<cant_resolve_binding>");
                    return;
                } 
                ITypeBinding superClass = binding.getSuperclass();
                if (superClass == null) {
                    System.out.println("<super_class_null>");
                    return;
                }
                System.out.println(superClass.getQualifiedName());
            } else {
                System.out.println("<no_super_class>");
            }
        } catch (Exception e) {
            System.out.println("<encounter_error>");
        }
    }
}
