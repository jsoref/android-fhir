// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.fhirengine.example;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.fhirengine.DaggerFhirEngineComponent;
import com.google.fhirengine.FhirEngine;
import com.google.fhirengine.ResourceAlreadyExistsException;

import org.hl7.fhir.r4.model.Library;
import org.opencds.cqf.cql.execution.EvaluationResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import ca.uhn.fhir.context.FhirContext;

public class MainActivity extends AppCompatActivity {

  private static final String DUMMY_ANC_LIBRARY =
      "https://raw.githubusercontent.com/who-int/anc-cds/develop/input/resources/library/library-ANCDummy.json";
  public static final String LIBRARY_ID = "Library/library-ANCDummy";

  FhirEngine fhirEngine;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final Button button = findViewById(R.id.load_cql_lib_button);
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        new DownloadAncLibrary().execute(DUMMY_ANC_LIBRARY);
      }
    });

    final Button evaluateButton = findViewById(R.id.evaluate_button);
    evaluateButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        new EvaluateAncLibrary().execute(LIBRARY_ID);
      }
    });

    // Gets FHIR Engine using Dagger component.
    fhirEngine = DaggerFhirEngineComponent.builder()
        .context(this).build().getFhirEngine();
  }

  private class DownloadAncLibrary extends AsyncTask<String, String, Void> {
    @Override
    protected Void doInBackground(String... strings) {
      String result = "";
      InputStream stream = null;
      try {
        stream = (InputStream) new URL(strings[0]).getContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = "";
        while (line != null) {
          result += line;
          line = reader.readLine();
        }

        FhirContext fhirContext = FhirContext.forR4();
        Library library = (Library) fhirContext.newJsonParser().parseResource(result);
        fhirEngine.save(library);
      } catch (IOException e) {
        e.printStackTrace();
      } catch (ResourceAlreadyExistsException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  private class EvaluateAncLibrary extends AsyncTask<String, String, Void> {
    @Override
    protected Void doInBackground(String... strings) {
      EvaluationResult evaluationResult = fhirEngine.evaluateCql("ANCFHIRDummy");
      return null;
    }
  }
}
