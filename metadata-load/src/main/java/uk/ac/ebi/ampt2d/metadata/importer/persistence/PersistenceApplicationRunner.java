/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package uk.ac.ebi.ampt2d.metadata.importer.persistence;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.ac.ebi.ampt2d.metadata.importer.SraRetrieverByAccession;
import uk.ac.ebi.ampt2d.metadata.importer.xml.SraXmlParser;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.AccessionVersionId;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.Study;
import uk.ac.ebi.ena.sra.xml.StudyType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PersistenceApplicationRunner implements ApplicationRunner {

    private static final String ACCESSION_FILE_PATH = "accessions.file.path";

    private static final Logger PERSISTENCE_APPLICATION_LOGGER =
            Logger.getLogger(PersistenceApplicationRunner.class.getName());

    private SraRetrieverByAccession sraRetrieverByAccession;

    private SraXmlParser<StudyType> sraStudyXmlParser;

    private Converter<StudyType, Study> sraToMetadataObjectConverter;

    private PagingAndSortingRepository<Study, AccessionVersionId> metadataStudyRepository;

    public PersistenceApplicationRunner(SraRetrieverByAccession sraRetrieverByAccession, SraXmlParser sraStudyXmlParser,
                                        PagingAndSortingRepository metadataStudyRepository,
                                        Converter<StudyType, Study> sraToMetadataObjectConverter) {
        this.sraRetrieverByAccession = sraRetrieverByAccession;
        this.sraStudyXmlParser = sraStudyXmlParser;
        this.metadataStudyRepository = metadataStudyRepository;
        this.sraToMetadataObjectConverter = sraToMetadataObjectConverter;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        Set<String> accessions = readAccessionsFromFile(arguments);

        for (String accession : accessions) {
            try {
                String xml = sraRetrieverByAccession.getXml(accession);
                StudyType studyType = sraStudyXmlParser.parseXml(xml, accession);
                Study study = sraToMetadataObjectConverter.convert(studyType);
                metadataStudyRepository.save(study);
            } catch (Exception exception) {
                PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Encountered Exception for accession"
                        + accession);
                PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, exception.getMessage());
            }
        }
    }

    private Set<String> readAccessionsFromFile(ApplicationArguments arguments) {
        List<String> analysisAccessionsFilePath = arguments.getOptionValues(ACCESSION_FILE_PATH);
        if (analysisAccessionsFilePath == null || analysisAccessionsFilePath.size() == 0) {
            PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Please provide accessions.file.path");
            throw new RuntimeException("Please provide accessions.file.path");
        }
        String accessionFilePath = analysisAccessionsFilePath.get(0);
        Set<String> analysisAccessions;
        try {
            analysisAccessions = new HashSet<>(Files.readAllLines(Paths.get(getClass().getClassLoader()
                    .getResource(accessionFilePath).toURI())));
        } catch (NullPointerException | URISyntaxException exception) {
            PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Provided file path is invalid");
            throw new RuntimeException("Provided file path is invalid/file does not exists");
        } catch (IOException exception) {
            PERSISTENCE_APPLICATION_LOGGER.log(Level.SEVERE, "Provided file is not valid/corrupt");
            throw new RuntimeException("Provided file is not valid/corrupt");
        }

        return analysisAccessions;
    }

}
