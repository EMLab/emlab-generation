/*******************************************************************************
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package emlab.gen.util;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static <T> List<T> asList(Iterable<T> iterable) {
        List<T> list;
        if (iterable instanceof List<?>) {
            list = (List<T>) iterable;
        } else {
            list = new ArrayList<T>();
            for (T t : iterable) {
                list.add(t);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
	public static <T, E extends T> List<E> asCastedList(Iterable<T> iterable) {
        List<E> list = new ArrayList<E>();
        for (T t : iterable) {
            list.add((E) t);
        }
        return list;
    }

    public static <E, T extends E> List<E> asDownCastedList(Iterable<T> iterable) {
        List<E> list = new ArrayList<E>();
        for (T t : iterable) {
            list.add((E) t);
        }
        return list;
    }

}
