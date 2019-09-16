/*******************************************************************************
 * Copyright 2019 grondag
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package grondag.fluidity.api.discrete;

import grondag.fluidity.api.storage.Port;

/**
 * The thing that will be used to take fluid in or out of another thing.
 */
public interface DiscretePort extends Port {
	<T> long accept(T article, long count, int flags);

    default <T> boolean acceptFrom(T article, long count, int flags, MutableDiscreteArticle<T> supply) {
        if (supply.article().equals(article) || supply.count() == 0) {
            final long result = accept(article, count, flags);
            if (result == 0) {
                return false;
            } else {
                supply.decrement(result);
                supply.setArticle(article);
                return true;
            }
        } else {
            return false;
        }
    }

    default <T> boolean acceptFrom(MutableDiscreteArticle<T> target, int flags) {
        if (target.count() == 0) {
            return false;
        } else {
            final long result = accept(target.article(), target.count(), flags);
            if (result == 0) {
                return false;
            } else {
            	// TODO: check flags?
                target.decrement(result);
                return true;
            }
        }
    }

    <T> long supply(T article, long count, int flags);


    default <T> boolean supplyTo(T article, long count, int flags, MutableDiscreteArticle<T> target) {
        if (target.article().equals(article) || target.count() == 0) {
            final long result = supply(article, count, flags);
            if (result == 0) {
                return false;
            } else {
            	//TODO: guards
                target.increment(result);
                target.setArticle(article);
                return true;
            }
        } else {
            return false;
        }
    }

    static DiscretePort VOID = new DiscretePort() {
        @Override
        public <T> long accept(T article, long count, int flags) {
            return 0;
        }

        @Override
        public <T> long supply(T article, long count, int flags) {
            return 0;
        }
    };
}
