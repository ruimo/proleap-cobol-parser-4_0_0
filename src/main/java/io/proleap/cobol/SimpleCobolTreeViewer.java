package io.proleap.cobol;

import io.proleap.cobol.CobolLexer;
import io.proleap.cobol.CobolParser;
import io.proleap.cobol.preprocessor.CobolPreprocessor;
import io.proleap.cobol.preprocessor.impl.CobolPreprocessorImpl;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.gui.Trees;
import org.apache.commons.io.FilenameUtils;
import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.params.impl.CobolParserParamsImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class SimpleCobolTreeViewer {
    
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java PreprocessedCobolTreeViewer <cobol-file> [options]");
            System.out.println("Options:");
            System.out.println("  --show-preprocessed : Show preprocessed source");
            System.out.println("  --copybook-dir <dir> : Specify copybook directory");
            System.out.println("  --format <format>    : Specify COBOL format (FIXED, FREE, VARIABLE)");
            System.exit(1);
        }
        
        String filePath = args[0];
        boolean showPreprocessed = false;
        String copybookDir = null;
        CobolPreprocessor.CobolSourceFormatEnum format = CobolPreprocessor.CobolSourceFormatEnum.FIXED;
        
        // コマンドライン引数の解析
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--show-preprocessed":
                    showPreprocessed = true;
                    break;
                case "--copybook-dir":
                    if (i + 1 < args.length) {
                        copybookDir = args[++i];
                    }
                    break;
                case "--format":
                    if (i + 1 < args.length) {
                        String formatStr = args[++i].toUpperCase();
                        try {
                            format = CobolPreprocessor.CobolSourceFormatEnum.valueOf(formatStr);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid format: " + formatStr);
                            System.err.println("Valid formats: FIXED, FREE, VARIABLE");
                            System.exit(1);
                        }
                        i++;
                    }
                    break;
            }
        }
        
        parseWithPreprocessing(filePath, showPreprocessed, copybookDir, format);
    }
    
    private static void parseWithPreprocessing(String filePath, boolean showPreprocessed, 
                                             String copybookDir, 
                                             CobolPreprocessor.CobolSourceFormatEnum format) throws IOException {
        
        CobolParserParams parm = new CobolParserParamsImpl();
        parm.setFormat(format);
        System.out.println("=== COBOL Parser with Preprocessing ===");
        System.out.println("File: " + filePath);
        System.out.println("Format: " + format);
        if (copybookDir != null) {
            System.out.println("Copybook Directory: " + copybookDir);
        }
        System.out.println();
        
        // ファイル読み込み
        Path inputPath = Paths.get(filePath);
        if (!Files.exists(inputPath)) {
            System.err.println("File not found: " + filePath);
            System.exit(1);
        }
        
        // プリプロセッサーの設定
        CobolPreprocessor preprocessor = new CobolPreprocessorImpl();
        
        // Copybookディレクトリの設定
        List<File> copyBookDirectories = new ArrayList<>();
        if (copybookDir != null) {
            File copybookDirFile = new File(copybookDir);
            if (copybookDirFile.exists() && copybookDirFile.isDirectory()) {
                copyBookDirectories.add(copybookDirFile);
                System.out.println("Added copybook directory: " + copybookDir);
            } else {
                System.err.println("Warning: Copybook directory not found or not a directory: " + copybookDir);
            }
        }
        
        // デフォルトのcopybookディレクトリも追加（入力ファイルと同じディレクトリ）
        File inputDir = inputPath.getParent().toFile();
        if (inputDir != null) {
            copyBookDirectories.add(inputDir);
            System.out.println("Added default copybook directory: " + inputDir.getAbsolutePath());
        }
        parm.setCopyBookDirectories(copyBookDirectories);
        
        try {
            System.out.println("\n=== Preprocessing ===");
            
            // プリプロセス実行
            String result = preprocessor.process(inputPath.toFile(), parm);
            
            if (result == null || result.trim().isEmpty()) {
                System.err.println("Preprocessing failed: Empty result");
                System.exit(1);
            }
            
            System.out.println("Preprocessing completed successfully");
            System.out.println("Original size: " + Files.readString(inputPath).length() + " characters");
            System.out.println("Preprocessed size: " + result.length() + " characters");
            
            // プリプロセス結果の表示（オプション）
            if (showPreprocessed) {
                System.out.println("\n=== Preprocessed Source ===");
                System.out.println(result);
                System.out.println("=== End of Preprocessed Source ===\n");
                
                // プリプロセス結果をファイルに保存
                String outputPath = getPreprocessedFilePath(filePath);
                Files.writeString(Paths.get(outputPath), result);
                System.out.println("Preprocessed source saved to: " + outputPath);
            }
            
            // パース処理
            System.out.println("\n=== Parsing ===");
            parseAndShowTree(result, filePath);
            
        } catch (Exception e) {
            System.err.println("Error during preprocessing or parsing:");
            e.printStackTrace();
            
            // プリプロセスに失敗した場合、元のファイルを直接パースしてみる
            System.err.println("\nAttempting to parse original file without preprocessing...");
            try {
                String originalContent = Files.readString(inputPath);
                parseAndShowTree(originalContent, filePath);
            } catch (Exception e2) {
                System.err.println("Failed to parse original file as well:");
                e2.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    private static void parseAndShowTree(String content, String originalFilePath) {
        try {
            // レキサーとパーサーのセットアップ
            CharStream input = CharStreams.fromString(content);
            CobolLexer lexer = new CobolLexer(input);
            
            // レキサーエラーリスナー
            lexer.removeErrorListeners();
            lexer.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg,
                                      RecognitionException e) {
                    System.err.println("Lexer error at " + line + ":" + charPositionInLine + " - " + msg);
                }
            });
            
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            CobolParser parser = new CobolParser(tokens);
            
            // パーサーエラーリスナー
            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg,
                                      RecognitionException e) {
                    System.err.println("Parser error at " + line + ":" + charPositionInLine + " - " + msg);
                }
            });
            
            // パース実行
            System.out.println("Starting parse...");
            ParseTree tree = parser.startRule();
            
            if (tree != null) {
                System.out.println("Parse completed successfully");
                System.out.println("Parse tree root: " + tree.getClass().getSimpleName());
                System.out.println("Number of children: " + tree.getChildCount());
                
                // ツリー表示
                System.out.println("\nOpening tree viewer...");
                Trees.inspect(tree, Arrays.asList(parser.getRuleNames()));
            } else {
                System.err.println("Parse failed: null tree returned");
            }
            
        } catch (Exception e) {
            System.err.println("Error during parsing:");
            e.printStackTrace();
        }
    }
    
    private static String getPreprocessedFilePath(String originalPath) {
        String baseName = FilenameUtils.getBaseName(originalPath);
        String extension = FilenameUtils.getExtension(originalPath);
        String directory = FilenameUtils.getFullPath(originalPath);
        
        return directory + baseName + "_preprocessed." + extension;
    }
}
