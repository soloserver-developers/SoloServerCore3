/*
 * Copyright 2022 Nafu Satsuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nafusoft.soloservercore;

import dev.nafusoft.soloservercore.exception.ItemProcessingException;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class InventoryEncoder {

    private InventoryEncoder() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    /**
     * Encodes the inventory into a byte array.
     *
     * @param inventory The inventory to encode.
     * @param compress  Whether to compress the inventory.
     * @return The encoded inventory.
     * @throws ItemProcessingException If an error occurs while processing the inventory.
     */
    public static byte[] encodeInventory(Inventory inventory, boolean compress) throws ItemProcessingException {
        try {
            // Serialize the item stack
            byte[] bytes;
            try (val outputStream = new ByteArrayOutputStream();
                 val dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(inventory.getSize());
                for (int i = 0; i < inventory.getSize(); i++) {
                    dataOutput.writeObject(inventory.getItem(i));
                }

                bytes = outputStream.toByteArray();
            }

            // Compress the bytes
            if (compress) {
                val deflater = new Deflater();
                try {
                    deflater.setInput(bytes);
                    deflater.finish();

                    try (val compressOutputStream = new ByteArrayOutputStream(bytes.length)) {
                        val buffer = new byte[1024];
                        while (!deflater.finished()) {
                            final int count = deflater.deflate(buffer);
                            compressOutputStream.write(buffer, 0, count);
                        }

                        bytes = compressOutputStream.toByteArray();
                    }
                } finally {
                    deflater.end();
                }
            }

            return bytes;
        } catch (Exception e) {
            throw new ItemProcessingException("Failed to encode inventory", e);
        }
    }

    /**
     * Encodes the inventory into a Base64 string.
     *
     * @param inventory The inventory to encode.
     * @param compress  Whether to compress the inventory.
     * @return The encoded inventory.
     * @throws ItemProcessingException If an error occurs while processing the inventory.
     */
    public static String encodeInventoryToString(Inventory inventory, boolean compress) throws ItemProcessingException {
        return Base64.getEncoder().encodeToString(encodeInventory(inventory, compress));
    }

    /**
     * Decodes the inventory from a byte array.
     *
     * @param inventoryBytes The inventory bytes to decode.
     * @param compressed     Whether the inventory bytes are compressed.
     * @return The decoded inventory.
     * @throws ItemProcessingException If an error occurs while processing the inventory.
     */
    public static Inventory decodeInventory(byte[] inventoryBytes, boolean compressed) throws ItemProcessingException {
        try {
            byte[] bytes = inventoryBytes;

            // Decompress the bytes
            if (compressed) {
                val inflater = new Inflater();
                try {
                    inflater.setInput(bytes);

                    try (val decompressOutputStream = new ByteArrayOutputStream(bytes.length)) {
                        val buffer = new byte[1024];
                        while (!inflater.finished()) {
                            final int count = inflater.inflate(buffer);
                            decompressOutputStream.write(buffer, 0, count);
                        }

                        bytes = decompressOutputStream.toByteArray();
                    }
                } finally {
                    inflater.end();
                }
            }

            try (val inputStream = new ByteArrayInputStream(bytes);
                 val dataInput = new BukkitObjectInputStream(inputStream)) {
                int size = dataInput.readInt();
                val inventory = Bukkit.createInventory(null, size, "");
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, (ItemStack) dataInput.readObject());
                }

                return inventory;
            }
        } catch (Exception e) {
            throw new ItemProcessingException("Failed to decode inventory", e);
        }
    }

    /**
     * Decodes the inventory from a Base64 string.
     *
     * @param string     The Base64 string to decode.
     * @param compressed Whether the inventory bytes are compressed.
     * @return The decoded inventory.
     * @throws ItemProcessingException If an error occurs while processing the inventory.
     */
    public static Inventory decodeInventoryFromString(String string, boolean compressed) throws ItemProcessingException {
        return decodeInventory(Base64.getDecoder().decode(string), compressed);
    }
}
