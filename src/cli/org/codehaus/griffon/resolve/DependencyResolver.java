/*
 * Copyright 2004-2010 the original author or authors.
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
 */
package org.codehaus.griffon.resolve;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ResolveReport;

import java.util.Set;

/**
 * An interface that defines methods to resolve dependencies based
 * on a supplied dependency definition.
 *
 * @author Graeme Rocher (Grails 1.2)
 */
public interface DependencyResolver {
    ResolveReport resolveDependencies();

    /**
     * Obtains the ModuleRevisionId instances for the given organisation name
     *
     * @param organisation The organisation name
     * @return The ModuleRevisionId
     */
    Set<ModuleRevisionId> getModuleRevisionIds(String organisation);
}
