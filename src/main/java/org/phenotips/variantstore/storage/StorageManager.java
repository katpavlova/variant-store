package org.phenotips.variantstore.storage;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.phenotips.variantstore.VariantStoreException;

/**
 * The StorageManager is responsible for managing the data files stored by the variant store.
 */
public class StorageManager {
    private Logger logger = Logger.getLogger(StorageManager.class);
    private Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private Path outDir;

    private static final List<String> supportedExtensions = Arrays.asList(".vcf", ".gz");
    private static final SuffixFileFilter fileFilter = new SuffixFileFilter(supportedExtensions, IOCase.INSENSITIVE);

    public StorageManager(Path outDir) {
        this.outDir = outDir;
    }

    /**
     * Add a file to the store. Currently only gzip or uncompressed files are supported.
     * After it is added to the datastore, the given VCF file is not required.
     *
     * @param filePath the path to the file.
     * @return a Future that completes when the file is in the datastore and is ready to be queried.
     * @throws VariantStoreException
     */
    public Future add(Path filePath) throws InvalidFileFormatException{

        // check for unsupported extensions
        if (!fileFilter.accept(filePath.toFile())) {
            throw new InvalidFileFormatException(
                    String.format("Supported VCF file extensions should be \"%s\",  encountered \"%s.\"",
                            StringUtils.join(supportedExtensions, "\" or \""),
                            FilenameUtils.getExtension(filePath.toString())
                    )
            );
        }

        FutureTask<String> task = new FutureTask<String>(new ProcessSingleVCFTask(filePath, outDir));
        executor.execute(task);
        return task;
    }

    public static SuffixFileFilter getSupportedFileFilter() {
        return fileFilter;
    }
}
