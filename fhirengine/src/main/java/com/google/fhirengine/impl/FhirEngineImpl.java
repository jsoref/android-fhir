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

package com.google.fhirengine.impl;

import com.google.fhirengine.FhirEngine;
import com.google.fhirengine.ResourceAlreadyExistsException;
import com.google.fhirengine.ResourceNotFoundException;
import com.google.fhirengine.cql.FhirEngineDataProvider;
import com.google.fhirengine.cql.FhirEngineLibraryLoader;
import com.google.fhirengine.db.Database;
import com.google.fhirengine.db.ResourceAlreadyExistsInDbException;
import com.google.fhirengine.db.ResourceNotFoundInDbException;
import com.google.fhirengine.resource.ResourceUtils;
import com.google.fhirengine.search.SearchEngine;
import com.google.fhirengine.search.SearchRequestBuilder;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.fhir.ucum.Search;
import org.hl7.fhir.r4.model.Resource;
import org.opencds.cqf.cql.data.DataProvider;
import org.opencds.cqf.cql.execution.CqlEngine;
import org.opencds.cqf.cql.execution.EvaluationResult;
import org.opencds.cqf.cql.execution.LibraryLoader;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/** Implementation of {@link FhirEngine}. */
public class FhirEngineImpl implements FhirEngine {

  private final Database database;
  private final CqlEngine cqlEngine;

  @Inject
  public FhirEngineImpl(Database database, LibraryLoader libraryLoader,
      Map<String, DataProvider> dataProviderMap) {
    this.database = database;
    this.cqlEngine = new CqlEngine(libraryLoader, dataProviderMap, null,
        EnumSet.noneOf(CqlEngine.Options.class));
  }

  @Override
  public <R extends Resource> void save(R resource) throws ResourceAlreadyExistsException {
    try {
      database.insert(resource);
    } catch (ResourceAlreadyExistsInDbException e) {
      throw new ResourceAlreadyExistsException(
          ResourceUtils.getResourceType(resource.getClass()).name(),
          resource.getId(), e);
    }
  }

  @Override
  public <R extends Resource> void update(R resource) {
    database.update(resource);
  }

  @Override
  public <R extends Resource> R load(Class<R> clazz, String id) throws ResourceNotFoundException {
    try {
      return database.select(clazz, id);
    } catch (ResourceNotFoundInDbException e) {
      throw new ResourceNotFoundException(ResourceUtils.getResourceType(clazz).name(), id, e);
    }
  }

  @Override
  public <R extends Resource> R remove(Class<R> clazz, String id) {
    throw new UnsupportedOperationException("Not implemented yet!");
  }

  @Override
  public EvaluationResult evaluateCql(String libraryVersionId) {
    return cqlEngine.evaluate(new VersionedIdentifier().withId(libraryVersionId));
  }
}
