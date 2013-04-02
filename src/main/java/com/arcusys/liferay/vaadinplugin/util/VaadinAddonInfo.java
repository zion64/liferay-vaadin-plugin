package com.arcusys.liferay.vaadinplugin.util;

/*
 * #%L
 * Liferay Vaadin Plugin
 * %%
 * Copyright (C) 2010 - 2013 Vaadin Ltd.
 * Copyright (C) 2013 Arcusys Ltd.
 * %%
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
 * #L%
 */

import java.io.File;
import java.util.List;

public class VaadinAddonInfo {

        private final String name;
        private final String version;
        private final File jarFile;
        private final List<String> widgetsets;

        public VaadinAddonInfo(String name, String version, File jarFile,
                               List<String> widgetsets) {
            this.name = name;
            this.version = version;
            this.jarFile = jarFile;
            this.widgetsets = widgetsets;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public File getJarFile() {
            return jarFile;
        }

        @Override
        public String toString() {
            return name + " " + version;
        }

        public List<String> getWidgetsets() {
            return widgetsets;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VaadinAddonInfo other = (VaadinAddonInfo) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }
}
