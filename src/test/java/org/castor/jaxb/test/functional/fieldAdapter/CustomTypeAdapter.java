/*
 * Copyright 2011 Jakub Narloch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.castor.jaxb.test.functional.fieldAdapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 */
public class CustomTypeAdapter extends XmlAdapter<CustomType, String> {

    @Override
    public String unmarshal(CustomType customType) throws Exception {
        return customType.getValue();
    }

    @Override
    public CustomType marshal(String value) throws Exception {
        CustomType customType = new CustomType();
        customType.setValue(value);
        return customType;
    }
}
