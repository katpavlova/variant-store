package org.phenotips.variantstore;

import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.vcf.VCFFileReader;
import htsjdk.variant.vcf.VCFHeader;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;
import org.ga4gh.GAVariant;
import org.phenotips.variantstore.models.Info;
import org.phenotips.variantstore.writers.InfoWriter;
import org.phenotips.variantstore.writers.VariantWriter;

/**
 * Created by meatcar on 1/19/15.
 */
public class ProcessVCFTask implements Callable {
    Logger logger = Logger.getLogger(ProcessVCFTask.class);
    private String outDir;
    File vcfFile;

    public ProcessVCFTask(String fileName, String outDir) {
        vcfFile = new File(fileName);
        this.outDir = outDir;
    }

    @Override
    public Object call() throws Exception {
        VCFFileReader vcfReader = new VCFFileReader(vcfFile, false);
        VCFHeader vcfHeader = vcfReader.getFileHeader();
        String id = null;

        if (vcfHeader.getSampleNamesInOrder().size() > 1) {
            throw new VariantStoreException("Multi-sample VCF unsupported");
        } else if (vcfHeader.getSampleNamesInOrder().size() == 1) {
            id = vcfHeader.getSampleNamesInOrder().get(0);
            //TODO: pass this to getGaVariant
        } else {
            //TODO: get patient name
        }

        Iterator<VariantContext> it = vcfReader.iterator();

        VariantWriter variantWriter = new VariantWriter(vcfFile.getName(), outDir);
        InfoWriter infoWriter = new InfoWriter(vcfFile.getName(), outDir);

        GAVariant gaVariant;
        Info typedInfo;

        VariantContext vcfRow;
        while (it.hasNext()) {
            try {
                vcfRow = it.next(); //.fullyDecode(vcfHeader, true);
            } catch (Exception e) {
                logger.error("Error encountered while processing " + vcfFile.getAbsolutePath());
                e.printStackTrace();
                continue;
            }

            /**
             * Parse VCF row to ga4gh schema + our own metadata schema
             * Write Parquet file
             */

            //TODO: remove file if it exists.
            variantWriter.write(vcfRow);
            infoWriter.write(vcfRow);
        }

        variantWriter.close();
        infoWriter.close();
        return null;
    }
}
