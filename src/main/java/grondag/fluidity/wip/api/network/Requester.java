/*******************************************************************************
 * Copyright 2019, 2020 grondag
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
package grondag.fluidity.wip.api.network;

/**
 * A player, device or process that initiates transactions and requests transfers and
 * other state changes of TransactionParticipants. For machines, often the device itself.
 *
 *
 * One purpose of Requester is to serve as an identity token that can be validated to
 * confirm the requests it is making are permissible.
 */
public interface Requester {

}
