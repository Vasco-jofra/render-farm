/* CONSTANT_String_Info.java
 * Part of BIT -- Bytecode Instrumenting Tool
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 * 
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 *  
 * By downloading BIT, the User agrees and acknowledges that in no event
 * will the Regents of the University of Colorado be liable for any
 * damages including lost profits, lost savings or other indirect,
 * incidental, special or consequential damages arising out of the use or
 * inability to use the BIT software.
 * 
 * BIT was invented by Han Bok Lee at the University of Colorado in
 * Boulder, Colorado.
 */


package BIT.BIT.lowBIT;

import java.io.*;

public class CONSTANT_String_Info extends Cp_Info {
  // data member
  // index of structure representing the sequence of characters
  // into the constant_pool table
  public short string_index;
  
  // constructor
  public CONSTANT_String_Info(DataInputStream iStream, byte tag) 
    throws IOException {
    this.tag = tag;
    string_index = (short) iStream.readUnsignedShort();
  }

  public CONSTANT_String_Info(short string_index) {
    tag = Constant.CONSTANT_String;
    this.string_index = string_index;
  }

  public void write(DataOutputStream oStream)
    throws IOException {
      oStream.writeByte((int) tag);
      oStream.writeShort((int) string_index);
  }

  public boolean equals(Object obj) {
    if (obj instanceof CONSTANT_String_Info)
      return (string_index == ((CONSTANT_String_Info) obj).string_index);
    return false;
  }
  public int size() { return 3; }

}

