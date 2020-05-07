package com.tenchael.cords;

import com.tenchael.cords.netty.CordsException;
import com.tenchael.cords.protocol.BulkReply;
import com.tenchael.cords.protocol.IntegerReply;
import com.tenchael.cords.protocol.MultiBulkReply;
import com.tenchael.cords.protocol.StatusReply;

public interface CordsServer {

    /**
     * Get the value of a key
     * String
     *
     * @param key0
     * @return BulkReply
     */
    BulkReply get(byte[] key0) throws CordsException;

    /**
     * Set the string value of a key
     * String
     *
     * @param key0
     * @param value1
     * @return StatusReply
     */
    StatusReply set(byte[] key0, byte[] value1) throws CordsException;


    /**
     * Echo the given string
     * Connection
     *
     * @param message0
     * @return BulkReply
     */
    BulkReply echo(byte[] message0) throws CordsException;

    /**
     * Ping the server
     * Connection
     *
     * @return StatusReply
     */
    StatusReply ping() throws CordsException;

    /**
     * Close the connection
     * Connection
     *
     * @return StatusReply
     */
    StatusReply quit() throws CordsException;

    /**
     * Delete a key
     * Generic
     *
     * @param key0
     * @return IntegerReply
     */
    IntegerReply del(byte[][] key0) throws CordsException;

    /**
     * Determine if a key exists
     * Generic
     *
     * @param key0
     * @return IntegerReply
     */
    IntegerReply exists(byte[] key0) throws CordsException;


    /**
     * Find all keys matching the given pattern
     * Generic
     *
     * @param pattern0
     * @return MultiBulkReply
     */
    MultiBulkReply keys(byte[] pattern0) throws CordsException;

}
