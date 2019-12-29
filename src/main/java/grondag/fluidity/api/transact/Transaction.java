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
package grondag.fluidity.api.transact;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import grondag.fluidity.impl.TransactionImpl;

@API(status = Status.EXPERIMENTAL)
public interface Transaction extends AutoCloseable {
	void rollback();

	void commit();

	<T extends TransactionParticipant> T enlist(T container);

	@Override
	void close();

	static Transaction open() {
		return TransactionImpl.open();
	}
}
