.class public Main
.super java/lang/Object


.method public static main([Ljava/lang/String;)V
.limit stack 128
.limit locals 128
		new Main
		invokespecial Main/<init>()V
		return
.end method


.method public static start(Ljava/lang/Boolean;Ljava/lang/Integer;)V
.limit stack 128
.limit locals 128
		aload 0
		invokevirtual java/lang/Boolean/booleanValue()Z
		iconst_1
		ixor
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		invokevirtual java/lang/Boolean/booleanValue()Z
		ifne Label_0
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		ldc 100
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		if_icmpgt Label_0
		goto Label_1
	Label_0:
		aload 0
		invokevirtual java/lang/Boolean/booleanValue()Z
		ifne Label_2
		getstatic java/lang/System/out Ljava/io/PrintStream;
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		ldc 30
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		imul
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		invokevirtual java/io/PrintStream/println(I)V
		goto Label_3
	Label_2:
		getstatic java/lang/System/out Ljava/io/PrintStream;
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		ldc 30
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		idiv
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		invokevirtual java/io/PrintStream/println(I)V
	Label_3:
	Label_1:
		invokestatic Main/itList()V
		return
.end method


.method public static itList()V
.limit stack 128
.limit locals 128
		new List
		dup
		new java/util/ArrayList
		dup
		invokespecial java/util/ArrayList/<init>()V
		astore 0
		aload 0
		ldc "ata"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		aload 0
		ldc "souri"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		aload 0
		ldc "javad"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		aload 0
		ldc "nemati"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		aload 0
		ldc "darabi"
		invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z
		pop
		aload 0
		invokespecial List/<init>(Ljava/util/ArrayList;)V
		ldc 0
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokestatic Main/printRecursive(LList;Ljava/lang/Integer;)V
		return
.end method


.method public static printRecursive(LList;Ljava/lang/Integer;)V
.limit stack 128
.limit locals 128
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		ldc 5
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		if_icmpeq Label_4
		goto Label_5
	Label_4:
		return
	Label_5:
		getstatic java/lang/System/out Ljava/io/PrintStream;
		aload 0
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		invokevirtual List/getElement(I)Ljava/lang/Object;
		checkcast java/lang/String
		invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V
		aload 0
		aload 1
		invokevirtual java/lang/Integer/intValue()I
		ldc 1
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokevirtual java/lang/Integer/intValue()I
		iadd
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokestatic Main/printRecursive(LList;Ljava/lang/Integer;)V
		return
.end method


.method public <init>()V
.limit stack 128
.limit locals 128
		aload_0
		invokespecial java/lang/Object/<init>()V
		ldc 1
		invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;
		ldc 300
		invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;
		invokestatic Main/start(Ljava/lang/Boolean;Ljava/lang/Integer;)V
		return
.end method
