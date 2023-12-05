package io.proleap.cobol.preprocessor.copy.extension.precedence.variable;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.proleap.cobol.asg.params.CobolParserParams;
import io.proleap.cobol.asg.params.impl.CobolParserParamsImpl;
import io.proleap.cobol.preprocessor.CobolPreprocessor.CobolSourceFormatEnum;
import io.proleap.cobol.preprocessor.impl.CobolPreprocessorImpl;

public class CopyPrecedenceTest {

	private static final String DIR = "src/test/resources/io/proleap/cobol/preprocessor/copy/extension/precedence/variable";

	@Test
	public void testCopyBookDirectories() throws Exception {
		final File copyBookDirectory = new File(DIR + "/copybooks");
		final List<File> copyBookDirectories = Arrays.asList(copyBookDirectory);

		final CobolParserParams params = new CobolParserParamsImpl();
		params.setCopyBookDirectories(copyBookDirectories);
		params.setCopyBookExtensions(Arrays.asList("someotherextension", "txt", "cbl"));
		params.setFormat(CobolSourceFormatEnum.FIXED);

		final File inputFile = new File(DIR + "/CopyPrecedence.cbl");
		final String preProcessedInput = new CobolPreprocessorImpl().process(inputFile, params);

		final File expectedFile = new File(DIR + "/CopyPrecedence.cbl.preprocessed");
		final String expected = Files.readString(expectedFile.toPath(), StandardCharsets.UTF_8);
		assertEquals(expected, preProcessedInput);
	}

	@Test
	public void testCopyBookFiles() throws Exception {
		final File copyBookFile1 = new File(DIR + "/copybooks/SomeCopyBook");
		final File copyBookFile2 = new File(DIR + "/copybooks/SomeCopyBook.cbl");
		final File copyBookFile3 = new File(DIR + "/copybooks/SomeCopyBook.txt");
		final List<File> copyBookFiles = Arrays.asList(copyBookFile1, copyBookFile2, copyBookFile3);

		final CobolParserParams params = new CobolParserParamsImpl();
		params.setCopyBookFiles(copyBookFiles);
		params.setCopyBookExtensions(Arrays.asList("someotherextension", "txt", "cbl"));
		params.setFormat(CobolSourceFormatEnum.FIXED);

		final File inputFile = new File(DIR + "/CopyPrecedence.cbl");
		final String preProcessedInput = new CobolPreprocessorImpl().process(inputFile, params);

		final File expectedFile = new File(DIR + "/CopyPrecedence.cbl.preprocessed");
		final String expected = Files.readString(expectedFile.toPath(), StandardCharsets.UTF_8);
		assertEquals(expected, preProcessedInput);
	}
}
