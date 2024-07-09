package tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringWrapper {

    private static final ArrayList<Integer> lineCharCountList = new ArrayList<>();

    /**
     * Wrap a paragraph
     * @param paragraph - input paragraph
     * @param wrapWidth - width of wrap
     * @return wrapped paraagraph
     */
//    public static String wrapParagraph(String paragraph, int wrapWidth) {
//        String TAG = "wrapText";
//
//        // Special cases
//        if (paragraph == null) return "";
//        if (paragraph.length() <= wrapWidth) {
//            lineCharCountList.add(paragraph.length()); // add the count of chars to the list
//            return paragraph;
//        }
//
//        // Normal cases
//        StringBuilder outStr = new StringBuilder();
//        ArrayList<String> words = new ArrayList<>(Arrays.asList(paragraph.split(" ")));
//
//        StringBuilder line = new StringBuilder();
//        while (!words.isEmpty()) {
//            if ((line + words.getFirst()).length() + 1 < wrapWidth) { // +1 for the last space
//                // add if doesn't exceed the limit
//                line.append(words.removeFirst()).append(" ");
//            } else {
//                outStr.append(line).append("\n");
//                // add the count of chars to the list
//                lineCharCountList.add(line.length());
//                // reset the line
//                line = new StringBuilder();
//            }
//        }
//        outStr.append(line); // append the last line (bc the words is now empty)
//        lineCharCountList.add(line.length()); // add the count of chars to the list
//
//        return outStr.toString();
//    }

    public static String wrapAndPadParagraph(String paragraph, int wrapWidth) {
        // Special cases
        if (paragraph == null) return "";
        if (paragraph.length() <= wrapWidth) return paragraph;

        // Normal cases
        StringBuilder outStr = new StringBuilder();
        ArrayList<String> words = new ArrayList<>(Arrays.asList(paragraph.split(" ")));

        StringBuilder line = new StringBuilder();
        while (!words.isEmpty()) {
            if ((line + words.getFirst()).length() + 1 < wrapWidth) { // Still room for more
                line.append(words.removeFirst()).append(" ");
            } else {
                // Pad the line to reach wrapWidth (-1 bc later \n is added)
                for (int c = 0; c < wrapWidth - line.length() - 1; c++) {
                    line.append(" ");
                }

                // Add the line to the output
                outStr.append(line).append("\n");

                // reset the line
                line = new StringBuilder();
            }
        }

        return outStr.toString();
    }

    /**
     * Manually wrap the text (add \n) and write to outFileName
//     * @param resTxtFileName - name of the text file
     * @param outFileName - name of the output file
     * @param wrapWidth - max width to wrap
     * @return ArrayList<Integer></> - number of chraacters in each line
     */
//    public static ArrayList<Integer> wrapFile(String resTxtFileName, String outFileName, int wrapWidth)
//            throws IOException {
//        String TAG = "wrapFile";
//
//        // Read and get the paragraphs
////        String filePath = System.getProperty("user.dir") + "/res/" + resTxtFileName;
//        String content = Files.readString(Path.of(resTxtFileName));
//        String[] paragraphs = content.split("\n");
//
//        // Wrap each paragraph and write to file
//        PrintWriter outFilePW = new PrintWriter(new FileWriter(outFileName));
//        int nParagraphs = paragraphs.length;
//        for (int pi = 0; pi < nParagraphs - 1; pi++) {
//            outFilePW.println(wrapParagraph(paragraphs[pi], wrapWidth));
//        }
//        outFilePW.print(wrapParagraph(paragraphs[nParagraphs - 1], wrapWidth)); // write the last paragraph
//        outFilePW.close();
//
//        return lineCharCountList;
//    }

//    public static ArrayList<Integer> wrapFile(URI txtFileURI, String outFileName, int wrapWidth)
//            throws IOException {
//
//        // Read and get the paragraphs
////        String filePath = System.getProperty("user.dir") + "/res/" + resTxtFileName;
//        String content = Files.readString(Path.of(txtFileURI));
//        String[] paragraphs = content.split("\n\n");
//
//        // Wrap each paragraph and write to file
//        PrintWriter outFilePW = new PrintWriter(new FileWriter(outFileName));
//        int nParagraphs = paragraphs.length;
//        for (int pi = 0; pi < nParagraphs - 1; pi++) {
//            outFilePW.println(wrapAndPadParagraph(paragraphs[pi], wrapWidth));
//            outFilePW.println();
//        }
//        outFilePW.print(wrapAndPadParagraph(paragraphs[nParagraphs - 1], wrapWidth)); // write the last paragraph
//        outFilePW.close();
//
//        return lineCharCountList;
//    }

//    public static int wrapFile(URI txtFileURI, String outFileName, int wrapWidth)
//            throws IOException {
//
//        // Read and get the paragraphs
//
//        String content = Files.readString(Path.of(txtFileURI));
//        String[] paragraphs = content.split("\n\n");
//
//        // Wrap each paragraph and write to file
//        PrintWriter outFilePW = new PrintWriter(new FileWriter(outFileName));
//        int nParagraphs = paragraphs.length;
//        for (int pi = 0; pi < nParagraphs - 1; pi++) {
//            outFilePW.println(wrapAndPadParagraph(paragraphs[pi], wrapWidth));
//            outFilePW.println();
//        }
//        outFilePW.print(wrapAndPadParagraph(paragraphs[nParagraphs - 1], wrapWidth)); // write the last paragraph
//        outFilePW.close();
//
//        return lineCharCountList;
//    }

    public static void wrapText(URI txtFileURI, String outFileName, int wrapWidth) {
        Path inputPath = Paths.get(txtFileURI);
        Path outputPath = Paths.get(outFileName);

        try {
            List<String> textLines = Files.readAllLines(inputPath);
            BufferedWriter writer = Files.newBufferedWriter(outputPath);

            for (String textLine : textLines) {
                if (!textLine.isEmpty()) {
                    String[] parts = textLine.split(" ");
                    ArrayList<String> words = new ArrayList<>(Arrays.asList(parts));
                    StringBuilder paragraph = new StringBuilder();
                    StringBuilder line = new StringBuilder();

                    while (words.size() > 0) {
                        if (line.length() + words.getFirst().length() < wrapWidth - 1) {
                            line.append(words.removeFirst()).append(" ");
                        } else {
                            padStringBuilder(line, wrapWidth);
                            paragraph.append(line).append("\n");
                            line.setLength(0); // Reset
                        }

                    }

                    writer.write(paragraph.toString());

                } else {
                    writer.write(padString(textLine, wrapWidth));
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private static List<String> wrapParagraph(String paragraph, int wrapWidth) {
//        List<String> wrappedLines = new ArrayList<>();
//        String[] words = paragraph.split(" ");
//
//        StringBuilder line = new StringBuilder();
//        for (String word : words) {
//            if (line.length() + word.length() + 1 > wrapWidth) {
//                wrappedLines.add(padLine(line.toString(), wrapWidth));
//                line.setLength(0);
//            }
//            if (line.length() > 0) {
//                line.append(" ");
//            }
//            line.append(word);
//        }
//        if (line.length() > 0) {
//            wrappedLines.add(padLine(line.toString(), wrapWidth));
//        }
//
//        return wrappedLines;
//    }



    private static String padString(String line, int wrapWidth) {
        StringBuilder paddedLine = new StringBuilder(line);
        while (paddedLine.length() < wrapWidth) {
            paddedLine.append(" ");
        }
        return paddedLine.toString();
    }

    private static void padStringBuilder(StringBuilder input, int width) {
        while (input.length() < width) {
            input.append(" ");
        }
    }

    public static int countLines(String fileName) {
        Path path = Paths.get(fileName);
        try {
            return (int) Files.lines(path).count();
        } catch (IOException e) {
            e.printStackTrace();
            return -1; // Return -1 in case of an error
        }
    }

    public static String getLine(String fileName, int lineInd) {
        Path path = Paths.get(fileName);
        try {
            return Files.readAllLines(path).get(lineInd - 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int countTrailingSpaces(String input) {
        int count = 0;
        for (int i = input.length() - 1; i >= 0; i--) {
            if (input.charAt(i) == ' ') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }
}
