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
package uk.ac.ebi.ampt2d.metadata.cucumber;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
public class StudySteps {

    @Autowired
    private MockMvc mockMvc;

    @When("I create a study$")
    public void createTestStudy() throws Exception {
        createTestStudyWithAccession("EGAS0001");
    }

    @When("I create a study with (.*) for accession$")
    public void createTestStudyWithAccession(String accession) throws Exception {
        CommonStates.setResultActions(postTestStudy(
                accession, 1, "test_human_study", false, LocalDate.now()
        ));
    }

    @When("^I request POST /studies with JSON-like payload:$")
    public void postTestStudy(String jsonLikeData) throws Exception {
        String[] values = jsonLikeData.split(",");
        String json = "{";
        for (String value : values) {
            if (value.contains("releaseDate")) {
                json += "\"releaseDate\": \"";
                if (jsonLikeData.contains("today")) {
                    json += LocalDate.now();
                } else if (jsonLikeData.contains("yesterday")) {
                    json += LocalDate.now().plusDays(-1);
                } else if (jsonLikeData.contains("tomorrow")) {
                    json += LocalDate.now().plusDays(+1);
                }
                json += "\",";
                continue;
            }
            json += value + ",";
        }
        json += "\"description\": \"Nothing important\"," +
                "\"center\": \"EBI\"}";

        CommonStates.setResultActions(mockMvc.perform(post("/studies")
                .with(CommonStates.getRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.getBytes())));
    }

    @When("^I request GET for the studies with query parameter (.*)")
    public void performGetOnResourcesQuery(String param) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies" + "?" + param)
                .with(CommonStates.getRequestPostProcessor())));
    }

    @When("^I request PATCH (.*) with patch and content (.*)")
    public void performPatchOnResourceWithContent(String urlKey, String content) throws Exception {
            CommonStates.setResultActions(mockMvc.perform(patch(CommonStates.getUrl(urlKey) + "/patch")
                    .with(CommonStates.getRequestPostProcessor())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(content)));
    }

    @When("^I request search studies having release (.*) today")
    public void performSearchOnResourcesWithBaseAndParametersAndDay(String parameter) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/release-date?"+parameter+"="+LocalDate.now())
                .with(CommonStates.getRequestPostProcessor())));
    }

    @When("^I request elaborate search with date range for the studies base (.*) and with the parameters: (\\d*)$")
    public void performSearchOnResourcesWithBaseAndParametersAndDayRange(String base, int day) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base+"?"+"from="+LocalDate.now().plusDays(day)+"&to="+LocalDate.now().plusDays(day))
                .with(CommonStates.getRequestPostProcessor())));
    }

    @When("^I request search for the studies with base (.*) and name (.*) value (.*)$")
    public void performSearchOnResourcesWithParameters(String base, String name, String value) throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/"+base).param(name, value)
                .with(CommonStates.getRequestPostProcessor())));
    }

    @When("^I request search for studies that have been released")
    public void performSearchOnResources() throws Exception {
        CommonStates.setResultActions(mockMvc.perform(get("/studies/search/release-date")
                .with(CommonStates.getRequestPostProcessor())));
    }

    @Then("^the response should contain one study$")
    public void checkResponseListSize() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$..studies").isArray())
                .andExpect(jsonPath("$..studies.length()").value(1));
    }

    @Then("^the response should contain field releaseDate$")
    public void checkResponseJsonFieldValueExist() throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$.releaseDate").exists());
    }

    @Then("^the response should contain field (.*) with a false boolean value$")
    public void checkResponseJsonFieldValueFalse(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).value(false));
    }

    @Then("^the response should not contain field (.*)$")
    public void checkResponseJsonNoField(String field) throws Exception {
        CommonStates.getResultActions().andExpect(jsonPath("$."+field).doesNotExist());
    }

    private ResultActions postTestStudy(String accession, int version, String name, boolean deprecated, LocalDate releaseDate) throws Exception {
        String jsonContent = "{" +
                "      \"accessionVersionId\": {" +
                "       \"accession\": \"" + accession +  "\"," +
                "        \"version\": " + version +
                "        }," +
                "      \"name\": \"" + name + "\"," +
                "      \"description\": \"Nothing important\"," +
                "      \"center\": \"EBI\"," +
                "      \"deprecated\": \"" + deprecated + "\"," +
                "      \"releaseDate\": \"" + releaseDate + "\"" +
                "    }";

        return mockMvc.perform(post("/studies")
                .with(CommonStates.getRequestPostProcessor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated());
    }
}
