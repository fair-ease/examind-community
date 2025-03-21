/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
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

package org.constellation.dto.service.config.csw;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="HarvestTasks")
@XmlAccessorType(XmlAccessType.FIELD)
public class HarvestTasks {

    private List<HarvestTask> task;

    public HarvestTasks() {

    }

    public HarvestTasks(List<HarvestTask> task) {
        this.task = task;
    }

    /**
     * @return the tasks
     */
    public List<HarvestTask> getTask() {
        if (task == null) {
            task = new ArrayList<>();
        }
        return task;
    }

    /**
     * @param task the tasks to set
     */
    public void setTask(List<HarvestTask> task) {
        this.task = task;
    }

    /**
     * @param task the tasks to set
     */
    public void addTask(HarvestTask task) {
        if (this.task == null) {
            this.task = new ArrayList<>();
        }
        this.task.add(task);
    }

    /**
     * Return a task from is sourceURL
     */
    public HarvestTask getTaskFromSource(String sourceURL) {
        if (task == null) {
            task = new ArrayList<>();
        }
        for (HarvestTask t: task) {
            if (t.getSourceURL() != null && t.getSourceURL().equals(sourceURL)) {
                return t;
            }
        }
        return null;
    }
}
