package com.arcusys.liferay.vaadinplugin.util;

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
