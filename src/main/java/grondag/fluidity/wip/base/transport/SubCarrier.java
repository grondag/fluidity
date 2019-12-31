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
package grondag.fluidity.wip.base.transport;

import java.util.function.Supplier;

import grondag.fluidity.api.storage.ArticleConsumer;
import grondag.fluidity.api.storage.ArticleSupplier;
import grondag.fluidity.wip.api.transport.Carrier;
import grondag.fluidity.wip.api.transport.CarrierConnector;
import grondag.fluidity.wip.api.transport.CarrierListener;
import grondag.fluidity.wip.api.transport.CarrierSession;
import grondag.fluidity.wip.api.transport.CarrierType;

public class SubCarrier extends BasicCarrier {
	protected AggregateCarrier parentCarrier = null;

	public SubCarrier(CarrierType carrierType) {
		super(carrierType);
	}

	public void setParent(AggregateCarrier parent) {
		parentCarrier = parent;
	}

	public AggregateCarrier getParent() {
		return parentCarrier;
	}

	@Override
	public Carrier effectiveCarrier() {
		return parentCarrier == null ? this : parentCarrier;
	}

	@Override
	protected void sendFirstListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onAttach(SubCarrier.this, a));
	}

	@Override
	protected void sendLastListenerUpdate(CarrierListener listener) {
		nodes.values().forEach(a -> listener.onDetach(SubCarrier.this, a));
	}

	@Override
	protected void onListenersEmpty() {
		// NOOP
	}

	@Override
	public CarrierType carrierType() {
		return carrierType;
	}

	@Override
	public void startListening(CarrierListener listener, boolean sendNotifications) {
		listeners.startListening(listener, sendNotifications);
	}

	@Override
	public void stopListening(CarrierListener listener, boolean sendNotifications) {
		listeners.stopListening(listener, sendNotifications);
	}

	@Override
	public CarrierSession attach(CarrierConnector fromNode, Supplier<ArticleConsumer> nodeConsumerFactory, Supplier<ArticleSupplier> nodeSupplierFactory) {
		final CarrierSessionImpl result = new CarrierSessionImpl(this, nodeConsumerFactory, nodeSupplierFactory);

		if(nodes.put(result.address, result) == null) {
			listeners.forEach(l -> l.onAttach(this, result));
		}

		return result;
	}

	@Override
	public void detach(CarrierSession node) {
		if(nodes.remove(node.nodeAddress()) != null) {
			listeners.forEach(l -> l.onDetach(this, node));
		}
	}

	@Override
	public int nodeCount() {
		return nodes.size();
	}

	@Override
	public Iterable<? extends CarrierSession> nodes() {
		return nodes.values();
	}
}