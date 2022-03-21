/*
 *    Examind community - An open source and standard compliant SDI
 *    https://community.examind.com/
 *
 * Copyright 2014 Geomatys.
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

package org.constellation.process.service;

import org.apache.sis.util.SimpleInternationalString;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.process.ExamindProcessFactory;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.InternationalString;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class RenameServiceDescriptor extends AbstractServiceDescriptor {

    public static final String NAME = "service.rename";
    public static final InternationalString ABSTRACT = new SimpleInternationalString("rename a new ogc service in constellation.");

    public static final String NEW_NAME_NAME = "newName";
    private static final String NEW_NAME_REMARKS = "new name of the service instance.";
    public static final ParameterDescriptor<String> NEW_NAME =  BUILDER
            .addName(NEW_NAME_NAME)
            .setRemarks(NEW_NAME_REMARKS)
            .setRequired(true)
            .create( String.class, null);

    /**Input parameters */
    public static final ParameterDescriptorGroup INPUT_DESC = BUILDER.addName("InputParameters").setRequired(true)
            .createGroup(SERVICE_TYPE, IDENTIFIER, NEW_NAME);

    /**
     * Public constructor use by the ServiceRegistry to find and instantiate all ProcessDescriptor.
     */
    public RenameServiceDescriptor() {
        super(NAME, ExamindProcessFactory.IDENTIFICATION, ABSTRACT, INPUT_DESC, EMPTY_OUTPUT_DESC);
    }

    @Override
    public AbstractCstlProcess buildProcess(ParameterValueGroup input) {
        return new RenameService(this, input);
    }
}
