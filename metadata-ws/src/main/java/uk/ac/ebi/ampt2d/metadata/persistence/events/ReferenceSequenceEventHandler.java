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
package uk.ac.ebi.ampt2d.metadata.persistence.events;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeLinkSave;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import uk.ac.ebi.ampt2d.metadata.exceptionhandling.ReferenceSequenceWithoutValidTaxonomyException;
import uk.ac.ebi.ampt2d.metadata.persistence.entities.ReferenceSequence;

@RepositoryEventHandler(ReferenceSequence.class)
public class ReferenceSequenceEventHandler {

    @HandleBeforeCreate
    @HandleBeforeSave
    public void validateTaxonomies(ReferenceSequence referenceSequence) {
        validateTaxonomyLink(referenceSequence);
    }

    @HandleBeforeLinkDelete
    @HandleBeforeLinkSave
    public void validateTaxonomies(ReferenceSequence referenceSequence, Object taxonomy) {
        validateTaxonomyLink(referenceSequence);
    }

    private void validateTaxonomyLink(ReferenceSequence referenceSequence) {
        if (referenceSequence.getTaxonomy() == null) {
            throw new ReferenceSequenceWithoutValidTaxonomyException();
        }
    }

}
