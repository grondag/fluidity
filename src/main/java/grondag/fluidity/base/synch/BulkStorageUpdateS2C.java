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
package grondag.fluidity.base.synch;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus.Experimental;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import grondag.fluidity.Fluidity;
import grondag.fluidity.api.article.Article;
import grondag.fluidity.api.fraction.Fraction;

@Experimental
public final class BulkStorageUpdateS2C {
	private BulkStorageUpdateS2C() {}

	public static PacketByteBuf begin(int count) {
		final PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
		buf.writeInt(count);
		return buf;
	}

	public static PacketByteBuf append(PacketByteBuf buf, Article article, Fraction amount, int handle) {
		article.toPacket(buf);
		amount.writeBuffer(buf);
		buf.writeVarInt(handle);
		return buf;
	}

	public static void sendFullRefresh(ServerPlayerEntity player, PacketByteBuf buf, Fraction capacity) {
		capacity.writeBuffer(buf);
		send(ID_FULL_REFRESH, player, buf);
	}

	public static void sendUpdateWithCapacity(ServerPlayerEntity player, PacketByteBuf buf, Fraction capacity) {
		capacity.writeBuffer(buf);
		send(ID_UPDATE_WITH_CAPACITY, player, buf);
	}

	public static void sendUpdate(ServerPlayerEntity player, PacketByteBuf buf) {
		send(ID_UPDATE, player, buf);
	}

	private static void send(Identifier id, ServerPlayerEntity player, PacketByteBuf buf) {
		final Packet<?> packet = ServerPlayNetworking.createS2CPacket(id, buf);
		player.networkHandler.sendPacket(packet);
	}

	public static Identifier ID_FULL_REFRESH = new Identifier(Fluidity.MOD_ID, "ffrs2c");
	public static Identifier ID_UPDATE = new Identifier(Fluidity.MOD_ID, "fuds2c");
	public static Identifier ID_UPDATE_WITH_CAPACITY = new Identifier(Fluidity.MOD_ID, "fucs2c");
}
