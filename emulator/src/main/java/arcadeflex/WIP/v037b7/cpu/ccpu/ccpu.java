/*** Glue Code (******************************************

Glue code to hook up Retrocade's CCPU emulator to MAME's
architecture.  Really, it's not so bad!

**********************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.WIP.v037b7.cpu.ccpu;

import static arcadeflex.WIP.v037b7.cpu.ccpu.ccpuH.*;
import static arcadeflex.WIP.v037b7.cpu.ccpu.ccputabl.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v037b7.mame.memoryH.*;

public class ccpu extends cpu_interface {
    
    public static int[] ccpu_icount = new int[1];

    public ccpu() {
        cpu_num = CPU_CCPU;
        num_irqs = 2;
        default_vector = 0;
        overclock = 1.0;
        no_int = 0;
        irq_int = -1;
        nmi_int = -1;
        address_shift = 0;
        address_bits = 15;
        endianess = CPU_IS_LE;
        align_unit = 1;
        max_inst_len = 3;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = ccpu_icount;
        ccpu_icount[0] = 1000;
    }

    @Override
    public void reset(Object param) {
        ccpu_reset(param);
    }

    @Override
    public void exit() {
        ccpu_exit();
    }

    @Override
    public int execute(int cycles) {
        return ccpu_execute(cycles);
    }

    @Override
    public Object init_context() {
        Object reg = new CONTEXTCCPU();
        return reg;
    }

    @Override
    public Object get_context() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_context(Object reg) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int[] get_cycle_table(int which) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_cycle_table(int which, int[] new_table) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int get_pc() {
        return ccpu_get_pc();
    }

    @Override
    public void set_pc(int val) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int get_sp() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_sp(int val) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int get_reg(int regnum) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_reg(int regnum, int val) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_nmi_line(int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_irq_line(int irqline, int linestate) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_irq_callback(irqcallbacksPtr callback) {
        ccpu_set_irq_callback(callback);
    }

    @Override
    public void internal_interrupt(int type) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void cpu_state_save(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void cpu_state_load(Object file) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String cpu_info(Object context, int regnum) {
        return ccpu_info(context, regnum);
    }

    @Override
    public int memory_read(int offset) {
        return cpu_readmem16(offset);
    }

    @Override
    public void memory_write(int offset, int data) {
        cpu_writemem16(offset, data);
    }

    @Override
    public int internal_read(int offset) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void internal_write(int offset, int data) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void set_op_base(int pc) {
        cpu_setOPbase16.handler(pc);
    }

        
/*TODO*///	static UINT8 ccpu_reg_layout[] = {
/*TODO*///		CCPU_PC, CCPU_CFLAG, CCPU_CSTATE, CCPU_A, CCPU_B, CCPU_I, -1,
/*TODO*///		CCPU_P, CCPU_J, CCPU_ACC, CCPU_CMP, CCPU_PA0, 0,
/*TODO*///	};
/*TODO*///	
/*TODO*///	/* Layout of the debugger windows x,y,w,h */
/*TODO*///	static UINT8 ccpu_win_layout[] = {
/*TODO*///		25, 0,55, 2,	/* register window (top rows) */
/*TODO*///		 0, 0,24,22,	/* disassembler window (left colums) */
/*TODO*///	    25, 3,55, 9,    /* memory #1 window (right, upper middle) */
/*TODO*///	    25,13,55, 9,    /* memory #2 window (right, lower middle) */
/*TODO*///	     0,23,80, 1,    /* command line window (bottom rows) */
/*TODO*///	};
/*TODO*///	
/*TODO*///	
/*TODO*///	/* the MAME version of the CCPU registers */
/*TODO*///	typedef struct ccpuRegs
/*TODO*///	{
/*TODO*///	    UINT16  accVal;
/*TODO*///	    UINT16  cmpVal;
/*TODO*///	    UINT8   pa0;
/*TODO*///	    UINT8   cFlag;
/*TODO*///	    UINT16  eRegPC;
/*TODO*///	    UINT16  eRegA;
/*TODO*///	    UINT16  eRegB;
/*TODO*///	    UINT16  eRegI;
/*TODO*///	    UINT16  eRegJ;
/*TODO*///	    UINT8   eRegP;
/*TODO*///	    UINT8   eCState;
/*TODO*///	} ccpuRegs;

	public static int CCPU_FETCH(int a) { return (cpu_readop(a+CCPU_PGM_OFFSET)); }
	public static int CCPU_READPORT(int a) { return (cpu_readport (a)); }
        public static void CCPU_WRITEPORT(int a, int v) { cpu_writeport (a,v); }

/*TODO*///	/*
/*TODO*///	 * Read a word from given RAM memory location
/*TODO*///	 */
/*TODO*///	#ifdef LSB_FIRST
/*TODO*///	#define CCPU_RAM_RDMEM(A) (unsigned)((cpu_readmem16((A<<1)+CCPU_DATA_OFFSET)<<8) | cpu_readmem16(((A<<1)+CCPU_DATA_OFFSET+1)))
/*TODO*///	#else
/*TODO*///	#define CCPU_RAM_RDMEM(A) (unsigned)((cpu_readmem16((A<<1)+CCPU_DATA_OFFSET)) | cpu_readmem16(((A<<1)+CCPU_DATA_OFFSET+1))<<8)
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 *	 Write a word to given RAM memory location
/*TODO*///	 */
/*TODO*///	#ifdef LSB_FIRST
/*TODO*///	#define CCPU_RAM_WRMEM(A,V) { cpu_writemem16(((A<<1)+CCPU_DATA_OFFSET+1),(V&0x0ff)); cpu_writemem16(((A<<1)+CCPU_DATA_OFFSET),((V>>8)&0x0ff)); }
/*TODO*///	#else
/*TODO*///	#define CCPU_RAM_WRMEM(A,V) { cpu_writemem16(((A<<1)+CCPU_DATA_OFFSET+1),((V>>8)&0x0ff)); cpu_writemem16(((A<<1)+CCPU_DATA_OFFSET),(V&0x0ff)); }
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	#define RAW_VECTORS 1
/*TODO*///	
/*TODO*///	/* this #define works around the naming conflict with the opcode_table
/*TODO*///	   in cinedbg.c */
/*TODO*///	#define opcode_table _opcode_table
/*TODO*///	
/*TODO*///	/* This prototype was missing */
/*TODO*///	extern void CinemaVectorData (int fromx, int fromy, int tox, int toy, int color);
/*TODO*///	
	
/*TODO*///	
/*TODO*///	
/*TODO*///	extern UINT16 ioSwitches;
/*TODO*///	extern UINT16 ioInputs;
	
	
	public void ccpu_reset(Object param)
	{
		cineReset();
	}
	
	public void ccpu_exit()
	{
		/* nothing to do ? */
	}
	
	public int ccpu_execute(int cycles)
	{
		int newCycles;
	
		newCycles = cineExec(cycles);
		return newCycles;
	}
	
	
/*TODO*///	unsigned ccpu_get_context(void *dst)
/*TODO*///	{
/*TODO*///	    if (dst != 0)
/*TODO*///	    {
/*TODO*///	        CONTEXTCCPU context;
/*TODO*///	        ccpuRegs *Regs = dst;
/*TODO*///	        cGetContext (&context);
/*TODO*///	        Regs.accVal = context.accVal;
/*TODO*///	        Regs.cmpVal = context.cmpVal;
/*TODO*///	        Regs.pa0 = context.pa0;
/*TODO*///	        Regs.cFlag = context.cFlag;
/*TODO*///	        Regs.eRegPC = context.eRegPC;
/*TODO*///	        Regs.eRegA = context.eRegA;
/*TODO*///	        Regs.eRegB = context.eRegB;
/*TODO*///	        Regs.eRegI = context.eRegI;
/*TODO*///	        Regs.eRegJ = context.eRegJ;
/*TODO*///	        Regs.eRegP = context.eRegP;
/*TODO*///	        Regs.eCState = context.eCState;
/*TODO*///	    }
/*TODO*///	    return sizeof(ccpuRegs);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	void ccpu_set_context(void *src)
/*TODO*///	{
/*TODO*///		if (src != 0)
/*TODO*///		{
/*TODO*///			CONTEXTCCPU context;
/*TODO*///			ccpuRegs *Regs = src;
/*TODO*///			context.accVal = Regs.accVal;
/*TODO*///			context.cmpVal = Regs.cmpVal;
/*TODO*///			context.pa0 = Regs.pa0;
/*TODO*///			context.cFlag = Regs.cFlag;
/*TODO*///			context.eRegPC = Regs.eRegPC;
/*TODO*///			context.eRegA = Regs.eRegA;
/*TODO*///			context.eRegB = Regs.eRegB;
/*TODO*///			context.eRegI = Regs.eRegI;
/*TODO*///			context.eRegJ = Regs.eRegJ;
/*TODO*///			context.eRegP = Regs.eRegP;
/*TODO*///			context.eCState = (CINESTATE)Regs.eCState;
/*TODO*///			cSetContext (&context);
/*TODO*///		}
/*TODO*///	}
	
	
	public int ccpu_get_pc()
	{
		CONTEXTCCPU context = new CONTEXTCCPU();
	
		cGetContext (context);
		return context.eRegPC.get();
	}
	
/*TODO*///	void ccpu_set_pc(unsigned val)
/*TODO*///	{
/*TODO*///		CONTEXTCCPU context;
/*TODO*///	
/*TODO*///		cGetContext (&context);
/*TODO*///		context.eRegPC = val;
/*TODO*///		cSetContext (&context);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned ccpu_get_sp(void)
/*TODO*///	{
/*TODO*///		CONTEXTCCPU context;
/*TODO*///	
/*TODO*///		cGetContext (&context);
/*TODO*///		return context.eRegP;	/* Is this a stack pointer? */
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ccpu_set_sp(unsigned val)
/*TODO*///	{
/*TODO*///		CONTEXTCCPU context;
/*TODO*///	
/*TODO*///		cGetContext (&context);
/*TODO*///		context.eRegP = val;   /* Is this a stack pointer? */
/*TODO*///		cSetContext (&context);
/*TODO*///	}
/*TODO*///	
/*TODO*///	unsigned ccpu_get_reg(int regnum)
/*TODO*///	{
/*TODO*///		CONTEXTCCPU context;
/*TODO*///		cGetContext (&context);
/*TODO*///	
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case CCPU_ACC: return context.accVal;
/*TODO*///			case CCPU_CMP: return context.cmpVal;
/*TODO*///			case CCPU_PA0: return context.pa0;
/*TODO*///			case CCPU_CFLAG: return context.cFlag;
/*TODO*///			case CCPU_PC: return context.eRegPC;
/*TODO*///			case CCPU_A: return context.eRegA;
/*TODO*///			case CCPU_B: return context.eRegB;
/*TODO*///			case CCPU_I: return context.eRegI;
/*TODO*///			case CCPU_J: return context.eRegJ;
/*TODO*///			case CCPU_P: return context.eRegP;
/*TODO*///			case CCPU_CSTATE: return context.eCState;
/*TODO*///	/* TODO: return contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///					return 0;
/*TODO*///		}
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ccpu_set_reg(int regnum, unsigned val)
/*TODO*///	{
/*TODO*///		CONTEXTCCPU context;
/*TODO*///	
/*TODO*///		cGetContext (&context);
/*TODO*///		switch( regnum )
/*TODO*///		{
/*TODO*///			case CCPU_ACC: context.accVal = val; break;
/*TODO*///			case CCPU_CMP: context.cmpVal = val; break;
/*TODO*///			case CCPU_PA0: context.pa0 = val; break;
/*TODO*///			case CCPU_CFLAG: context.cFlag = val; break;
/*TODO*///			case CCPU_PC: context.eRegPC = val; break;
/*TODO*///			case CCPU_A: context.eRegA = val; break;
/*TODO*///			case CCPU_B: context.eRegB = val; break;
/*TODO*///			case CCPU_I: context.eRegI = val; break;
/*TODO*///			case CCPU_J: context.eRegJ = val; break;
/*TODO*///			case CCPU_P: context.eRegP = val; break;
/*TODO*///			case CCPU_CSTATE: context.eCState = val; break;
/*TODO*///	/* TODO: set contents of [SP + wordsize * (REG_SP_CONTENTS-regnum)] */
/*TODO*///			default:
/*TODO*///				if( regnum <= REG_SP_CONTENTS )
/*TODO*///				{
/*TODO*///					unsigned offset = /* SP? + */ (REG_SP_CONTENTS-regnum);
/*TODO*///					(void)offset;
/*TODO*///				}
/*TODO*///	    }
/*TODO*///		cSetContext (&context);
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	void ccpu_set_nmi_line(int state)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
/*TODO*///	
/*TODO*///	void ccpu_set_irq_line(int irqline, int state)
/*TODO*///	{
/*TODO*///		/* nothing to do */
/*TODO*///	}
	
	public void ccpu_set_irq_callback(irqcallbacksPtr callback)
	{
		/* nothing to do */
	}

	public String ccpu_info(Object context, int regnum)
	{
/*TODO*///		static char buffer[16][47+1];
/*TODO*///		static int which = 0;
/*TODO*///		CONTEXTCCPU *r = context;
/*TODO*///	
/*TODO*///		which = ++which % 16;
/*TODO*///	    buffer[which][0] = '\0';
/*TODO*///		if( !context )
/*TODO*///		{
/*TODO*///	        static CONTEXTCCPU tmp;
/*TODO*///			cGetContext(&tmp);
/*TODO*///			r = &tmp;
/*TODO*///		}
/*TODO*///	
	    switch( regnum )
            {
/*TODO*///	        case CPU_INFO_REG+CCPU_PC: sprintf(buffer[which], "PC:%04X", r.eRegPC); break;
/*TODO*///			case CPU_INFO_REG+CCPU_CFLAG: sprintf(buffer[which], "C:%02X", r.cFlag); break;
/*TODO*///	        case CPU_INFO_REG+CCPU_CSTATE: sprintf(buffer[which], "S:%X", r.eCState); break;
/*TODO*///			case CPU_INFO_REG+CCPU_A: sprintf(buffer[which], "A:%03X", r.eRegA); break;
/*TODO*///			case CPU_INFO_REG+CCPU_B: sprintf(buffer[which], "B:%03X", r.eRegB); break;
/*TODO*///			case CPU_INFO_REG+CCPU_I: sprintf(buffer[which], "I:%03X", r.eRegI); break;
/*TODO*///	        case CPU_INFO_REG+CCPU_P: sprintf(buffer[which], "P:%X", r.eRegP); break;
/*TODO*///			case CPU_INFO_REG+CCPU_J: sprintf(buffer[which], "J:%03X", r.eRegJ); break;
/*TODO*///			case CPU_INFO_REG+CCPU_ACC: sprintf(buffer[which], "ACC:%03X", r.accVal); break;
/*TODO*///	        case CPU_INFO_REG+CCPU_CMP: sprintf(buffer[which], "CMP:%03X", r.cmpVal); break;
/*TODO*///	        case CPU_INFO_REG+CCPU_PA0: sprintf(buffer[which], "PA0:%02X", r.pa0); break;
/*TODO*///				break;
/*TODO*///			case CPU_INFO_FLAGS:
/*TODO*///				/* TODO: no idea how the flags should look like */
/*TODO*///				sprintf(buffer[which], "%c-%c%c%c%c",
/*TODO*///					(r.cFlag) ? 'C' : 'c',
/*TODO*///	                (r.eCState == state_A || r.eCState == state_AA) ? 'A':' ',
/*TODO*///	                (r.eCState == state_A) ? 'A':' ',
/*TODO*///	                (r.eCState == state_B || r.eCState == state_BB) ? 'B':' ',
/*TODO*///	                (r.eCState == state_B) ? 'B':' ');
/*TODO*///	            break;
			case CPU_INFO_NAME: return "CCPU";
			case CPU_INFO_FAMILY: return "Cinematronics CPU";
			case CPU_INFO_VERSION: return "1.0";
			case CPU_INFO_FILE: return "ccpu.java";
			case CPU_INFO_CREDITS: return "Copyright 1997/1998 Jeff Mitchell and the Retrocade Alliance\nCopyright 1997 Zonn Moore";
/*TODO*///			case CPU_INFO_REG_LAYOUT: return (const char *)ccpu_reg_layout;
/*TODO*///			case CPU_INFO_WIN_LAYOUT: return (const char *)ccpu_win_layout;
	    }
/*TODO*///		return buffer[which];
            throw new UnsupportedOperationException("Not supported yet.");
	}
	
/*TODO*///	/* TODO: hook up the disassembler */
/*TODO*///	unsigned ccpu_dasm(char *buffer, unsigned pc)
/*TODO*///	{
/*TODO*///	#ifdef MAME_DEBUG
/*TODO*///		return DasmCCPU(buffer,pc);
/*TODO*///	#else
/*TODO*///	    sprintf( buffer, "$%02X", cpu_readop(pc) );
/*TODO*///		return 1;
/*TODO*///	#endif
/*TODO*///	}
	
	public static void ccpu_Config (int jmi, int msize, int monitor)
	{
		cineSetJMI (jmi);
		cineSetMSize (msize);
		cineSetMonitor (monitor);
	}
	
	
/*TODO*///	void ccpu_SetInputs(int inputs, int switches)
/*TODO*///	{
/*TODO*///	/*	ioInputs = inputs;
/*TODO*///		ioSwitches = switches;*/
/*TODO*///	}
/*TODO*///	
/*TODO*///	
/*TODO*///	/* To do:
/*TODO*///	  - make RAM external */
/*TODO*///	
/*TODO*///	
/*TODO*///	/*============================================================================================*
/*TODO*///	
/*TODO*///		BELOW LIES THE CORE OF THE CCPU. THE CODE WAS KINDLY GIVEN TO MAME BY ZONN MOORE,
/*TODO*///		JEFF MITCHELL, AND NEIL BRADLEY. I HAVE PRETTY HEAVILY CLEANED IT UP.
/*TODO*///	
/*TODO*///	 *============================================================================================*/
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * cinecore.c, cinedbg.c, cineops.c -- Cinematronics game emulator
/*TODO*///	 * copyright 1997/1998 Jeff Mitchell and the Retrocade Alliance
/*TODO*///	 * copyright 1997 Zonn Moore
/*TODO*///	 *
/*TODO*///	 * This Cinematronics emulator may be freely used in any non-commercial
/*TODO*///	 * venture, and is to be considered as under the Gnu Public Licence
/*TODO*///	 * (GPL, copyleft). To avoid a huge deluge of email, I also allow the
/*TODO*///	 * MAME team usage under the so-called MAME licence for non-commercial
/*TODO*///	 * use.
/*TODO*///	 *
/*TODO*///	 * There are some restrictions, however, mostly to help further development
/*TODO*///	 * of the emulator and continue the enjoyment these fine games have
/*TODO*///	 * provided.
/*TODO*///	 *
/*TODO*///	 * 1) Jeff Mitchell (skeezix@skeleton.org) is the authoritative maintainer for
/*TODO*///	 *    the C implementation of the "CCPU" written by Zonn Moore. Modifications
/*TODO*///	 *    or changes to this code may not be distributed in source form, in whole
/*TODO*///	 *    or in part, without the written permission of the maintainer. (ie: At
/*TODO*///	 *    some point after changes have been made, submit them to the maintainer
/*TODO*///	 *    for inclusion into the authoritative source tree, for all to use.)
/*TODO*///	 * 2) Credit must be given where appropriate: Jeff Mitchell, author of the
/*TODO*///	 *    C version. Zonn Moore for the ASM version and all original research,
/*TODO*///	 *    original documentation, etc. (The C version is a rewrite of Zonn's
/*TODO*///	 *    brilliant work.) Neil Bradley, for the 32bit conversion of the ASM
/*TODO*///	 *    core, and technical assistance. Without him, the C version wouldn't
/*TODO*///	 *    have been written. Where possible, please include references to the
/*TODO*///	 *    official website for the software (see below), so that others may
/*TODO*///	 *    easily find the sources.
/*TODO*///	 * 3) Users of this software are encouraged to periodically check the
/*TODO*///	 *    official website for new revisions, bugfixes, etc. This is an
/*TODO*///	 *    ongoing project, with many optimizations yet to be done.
/*TODO*///	 *
/*TODO*///	 * Games known to work 100% or nearly so, at some point or another of
/*TODO*///	 * the emulators development (but not necessarily right now :):
/*TODO*///	 * RipOff, Solar Quest, Spacewar, Speed Freak, Star Castle, Star Hawk,
/*TODO*///	 * Tail Gunner, War of the Worlds, Warrior
/*TODO*///	 *
/*TODO*///	 * For reference, all of the cinematronics games are:
/*TODO*///	 * Armor Attack, Barrier, Boxing Bugs, Demon, Ripoff, Solar Quest,
/*TODO*///	 * Spacewar, Speed Freak, Star Castle, Star Hawk, Sundance, Tail Gunner
/*TODO*///	 * War of the worlds, Warrior
/*TODO*///	 * (There is another, but it has not been made available yet)
/*TODO*///	 *
/*TODO*///	 * USAGE:
/*TODO*///	 * 1) The emulator works like any other "processor" emulator
/*TODO*///	 * 2) It does expect a few variables to be set, however, indicating
/*TODO*///	 *    which switches were connected on the original boards. (Most
/*TODO*///	 *    important is the variable to chose whethor or not to use the MI
/*TODO*///	 *    or EI flag...). See below.
/*TODO*///	 * 3) The emulator expects the memory to ALREADY be interleaved into
/*TODO*///	 *    "normalacy". The cinem ROMs are slow to read otherwise...
/*TODO*///	 * 4) Endianness doesn't matter; its code nice and slowly :)
/*TODO*///	 * 5) Compile time options may be set to obtain debugging tool, runtime
/*TODO*///	 *    traces, etc etc.
/*TODO*///	 * 6) The emulator does the vector handling; you need provide a routine
/*TODO*///	 *    to receive the vector draw requests, so that it may queue them up
/*TODO*///	 * 7) Zonn will be documenting the method by which the game palettes
/*TODO*///	 *    may be derived, where applicable.
/*TODO*///	 *
/*TODO*///	 * INITIALIZATION:
/*TODO*///	 * Take, for example, the game RipOff (the reaosn I got into this mess :).
/*TODO*///	 * The following variables need to be set, and the functions called, in
/*TODO*///	 * order to have the "cpu" run properly (courtesy Neil Bradley, Zonn Moore):
/*TODO*///	 * bFlipX = 0;  bFlipY = 1;     bSwapXY = 0;     (for vector generation)
/*TODO*///	 * ioInputs = 0xffff; ioSwitches = 0xfff0;  (cleared values)
/*TODO*///	 * bOverlay = 0xff;                                 (No overlay)
/*TODO*///	 * cineSetJMI(1);                                      (JMI Allowed)
/*TODO*///	 * cineSetMSize(0);                            (8K)
/*TODO*///	 * cineSetMonitor(0);                       (Bi-level display)
/*TODO*///	 *
/*TODO*///	 * If masking values are needed by anyone for the various game
/*TODO*///	 * controllers and whatnot, let me know, and I'll put up a document.
/*TODO*///	 *
/*TODO*///	 * SUPPORT:
/*TODO*///	 * FTP and WEB:
/*TODO*///	 *   Official support archive is at ftp.retrocade.com and www.retrocade.com.
/*TODO*///	 *   Various mirrors will be kept (ie: skeleton.org, the Emulation Repository,
/*TODO*///	 *   erc.), and are yet to be announced.
/*TODO*///	 * EMAIL:
/*TODO*///	 *   The C version: Jeff Mitchell is skeezix@skeleton.org
/*TODO*///	 *   The ASM version: Neil Bradley (neil@synthcom.com) and
/*TODO*///	 *                    Zonn Moore (zonn@zonn.com)
/*TODO*///	 *
/*TODO*///	 * An emulator for the Cinematronics vector game hardware, originally
/*TODO*///	 * for use in the Retrocade emulator project. All work is based on
/*TODO*///	 * documentation and sources and testbeds created by Zonn. He's a freak.
/*TODO*///	 *
/*TODO*///	 * Last modified: 02/17/98
/*TODO*///	 *
/*TODO*///	 * 12/04/97:      Created a "SETFC" macro for storing the flag_C var
/*TODO*///	 *                (shift and mask it at inspection time)
/*TODO*///	 * 12/??/97:      Fixed subtraction bugs (using register_B for A0 instead
/*TODO*///	 *                of register_A). Fixed input mask value in opINP.
/*TODO*///	 * 12/24/97:      Added #define for CCPUSSTEP for single stepping
/*TODO*///	 * 12/25/97:      Added ioSwitches &= (!SW_ABORT) into the top
/*TODO*///	 *                of the cineExec call. This fixed Star Castle, but
/*TODO*///	 *                broke others :/
/*TODO*///	 *                  Made opLSLe_A_.. handle multiple consecutive occurances
/*TODO*///	 *                so things would be a little faster, and so DOS versus
/*TODO*///	 *                Unix traces would be easier.
/*TODO*///	 *                  Changed above ioSwitches fix to ~SW_ABORT instead. This
/*TODO*///	 *                rebroke StarCastle and Sundance, but fixed Barrier. *sigh*
/*TODO*///	 */
/*TODO*///	
/*TODO*///	/* Optional #defines for debugging:
/*TODO*///	 * CCPUBREAK -- causes stopage on unknown upcodes
/*TODO*///	 * For these to operate, the debugging variables must be set as well.
/*TODO*///	 * This allows runtime selection of traces, etc.
/*TODO*///	 * CCPUSSTEP -- force single stepping
/*TODO*///	 *
/*TODO*///	 * Debug Variables:
/*TODO*///	 * ccpubreak -- set to non-zero to enable "break"ing
/*TODO*///	 */
/*TODO*///	
/*TODO*///	/* 1) Need to macro-ize everything, so that I can have this whole
/*TODO*///	 *    source file written by a perl script generator easier.
/*TODO*///	 * 4) Any of the jumps weird? JNC?
/*TODO*///	 * 5) JEI's all fucked? Are the tredirector's set right in the first place?
/*TODO*///	 *    What about all those damned JPP8, 16 and 32s? They work right?
/*TODO*///	 * 6) Store ccpu_jmi_dip and other new state vars in struct?
/*TODO*///	 * 7) Various OUT functions correct?
/*TODO*///	 */
/*TODO*///	
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * Use 0xF000 so as to keep the current page, since it may well
/*TODO*///	 * have been changed with JPP.
/*TODO*///	 */
/*TODO*///	#define JMP() register_PC = ((register_PC - 1) & 0xF000) + register_J; ccpu_icount -= 2
/*TODO*///	
/*TODO*///	/* Declare needed macros */
/*TODO*///	#ifdef macintosh
/*TODO*///	#define UNFINISHED(x)  { SysBeep (0); }
/*TODO*///	#else
/*TODO*///	#define UNFINISHED(x)  { logerror("UNFINISHED: %s\n", x); }
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	/* Handy new operators ... */
/*TODO*///	
/*TODO*///	/* for Zonn translation :) */
/*TODO*///	#define SAR(var,arg)    (((signed short int) var) >> arg)

        /* for setting/checking the A0 flag */
	public static void SETA0(int var) {acc_a0 = new CINEBYTE(var); }
	public static CINEBYTE GETA0() { return acc_a0; }

	/* for setting/checking the Carry flag */
	public static void SETFC(int val) { flag_C = new CINEWORD(val); }
	public static CINEBYTE GETFC() { return new CINEBYTE(((flag_C.get() >> 8) & 0xFF) ); }

	static boolean bailOut = false;
	
	/* C-CPU context information begins --  */
	static CINEWORD register_PC = new CINEWORD(0); /* C-CPU registers; program counter */
	static CINEWORD register_A = new CINEWORD(0);  /* A-Register (accumulator) */
	static CINEWORD register_B = new CINEWORD(0);  /* B-Register (accumulator) */
	static CINEWORD register_I = new CINEWORD(0);  /* I-Register (last access RAM location) */
	static CINEWORD register_J = new CINEWORD(0);  /* J-Register (target address for JMP opcodes) */
	static CINEBYTE register_P = new CINEBYTE(0);  /* Page-Register (4 bits) */
	static CINEWORD FromX = new CINEWORD(0);       /* X-Register (start of a vector) */
	static CINEWORD FromY = new CINEWORD(0);       /* Y-Register (start of a vector) */
	static CINEWORD register_T = new CINEWORD(0);  /* T-Register (vector draw length timer) */
	static CINEWORD flag_C = new CINEWORD(0);      /* C-CPU flags; carry. Is word sized, instead
                                                        * of CINEBYTE, so we can do direct assignment
                                                        * and then change to BYTE during inspection.
                                                        */
	
	static CINEWORD cmp_old = new CINEWORD(0);     /* last accumulator value */
	static CINEWORD cmp_new = new CINEWORD(0);     /* new accumulator value */
	static CINEBYTE acc_a0 = new CINEBYTE(0);      /* bit0 of A-reg at last accumulator access */
	
	static int state = state_A;/* C-CPU state machine current state */
	
/*TODO*///	//static CINEWORD  ram[256];       /* C-CPU ram (for all pages) */
/*TODO*///	//#define CCPU_RAM_RDMEM(a) (ram[a])
/*TODO*///	//#define CCPU_RAM_WRMEM(a,v) (ram[a]=v)

	static int ccpu_jmi_dip = 0;     /* as set by cineSetJMI */
	static int ccpu_msize = 0;       /* as set by cineSetMSize */
	static int ccpu_monitor = 0;     /* as set by cineSetMonitor */
	
/*TODO*///	static CINEBYTE vgShiftLength = 0; /* number of shifts loaded into length reg */
/*TODO*///	static CINEWORD vgColour = 0;
/*TODO*///	/* -- Context information ends. */
/*TODO*///	
/*TODO*///	
/*TODO*///	int bNewFrame;
/*TODO*///	
/*TODO*///	/* Note: I have removed all of this assuming that the vector drawing function can handle things */
/*TODO*///	#if !RAW_VECTORS
/*TODO*///	int bFlipX;
/*TODO*///	int bFlipY;
/*TODO*///	int bSwapXY;
/*TODO*///	int bOverlay;
/*TODO*///	
/*TODO*///	extern int sdwGameXSize;
/*TODO*///	extern int sdwGameYSize;
/*TODO*///	extern int sdwXOffset;
/*TODO*///	extern int sdwYOffset;
/*TODO*///	#endif
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/* functions */
	
	
	/*
	 * cineExec() is what performs all the "processors" work; it will
	 * continue to execute until something horrible happens, a watchpoint
	 * is hit, cycle count exceeded, or other happy things.
	 */
        
        public static abstract interface opcode_func {
            public abstract int handler(int opcode);
        }
	
	public int cineExec (int /*CINELONG*/ cycles)
	{
		ccpu_icount[0] = cycles;
		bailOut = false;
	
	   	do
	   	{
	   		int opcode;
	
/*TODO*///			CALL_MAME_DEBUG;
			/*
			 * goto the correct piece of code
			 * for the current opcode. That piece of code will set the state
			 * for the next run, as well.
			 */
	
			opcode = CCPU_FETCH (register_PC.get());
                        register_PC.set(register_PC.get()+1);
			state = (cineops[state][opcode]).handler(opcode);
                        ccpu_icount[0] -= ccpu_cycles[opcode];
	
	
			/*
			 * the opcode code has set a state and done mischief with flags and
			 * the program counter; now jump back to the top and run through another
			 * opcode.
			 */
			if (bailOut != false)
	/*			ccpu_ICount = 0; */
				ccpu_icount[0] -= 100;
		}
		while (ccpu_icount[0] > 0);
	
		return cycles - ccpu_icount[0];
	}
	
	
	/*
	 * the actual opcode code; each piece should be careful to
	 * (1) set the correct state
	 * (2) increment the program counter as necessary
	 * (3) piss with the flags as needed
	 * otherwise the next opcode will be completely buggered.
	 */
	
	public static opcode_func opINP_A_AA  = new opcode_func() {
            @Override
            public int handler(int opcode) {
		/*
		 * bottom 4 bits of opcode are the position of the bit we want;
		 * obtain input value, shift over that no, and truncate to last bit.
		 * NOTE: Masking 0x07 does interesting things on Sundance and
		 * others, but masking 0x0F makes RipOff and others actually work :)
		 */
	
		cmp_new = new CINEWORD((CCPU_READPORT (CCPU_PORT_IOINPUTS) >> (opcode & 0x0F)) & 0x01);
	
		SETA0 (register_A.get());               /* save old accA bit0 */
		SETFC (register_A.get());
	
		cmp_old = register_A;               /* save old accB */
		register_A = cmp_new;               /* load new accB; zero other bits */
	
		return state_AA;
            }
        };

        	public static opcode_func opINP_B_AA  = new opcode_func() {
            @Override
            public int handler(int opcode) {
                throw new UnsupportedOperationException("Not supported yet.");
            
/*TODO*///		/*
/*TODO*///		 * bottom 3 bits of opcode are the position of the bit we want;
/*TODO*///		 * obtain Switches value, shift over that no, and truncate to last bit.
/*TODO*///		 */
/*TODO*///	
/*TODO*///		cmp_new = (CCPU_READPORT (CCPU_PORT_IOSWITCHES) >> (opcode & 0x07)) & 0x01;
/*TODO*///	
/*TODO*///		SETA0 (register_A);               /* save old accA bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_B;               /* save old accB */
/*TODO*///		register_B = cmp_new;               /* load new accB; zero other bits */
/*TODO*///	
/*TODO*///		return state_AA;
            }
        };
	
public static opcode_func opOUTsnd_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (!(register_A & 0x01))
/*TODO*///			CCPU_WRITEPORT (CCPU_PORT_IOOUTPUTS, CCPU_READPORT (CCPU_PORT_IOOUTPUTS) | (0x01 << (opcode & 0x07)));
/*TODO*///		else
/*TODO*///			CCPU_WRITEPORT (CCPU_PORT_IOOUTPUTS, CCPU_READPORT (CCPU_PORT_IOOUTPUTS) & ~(0x01 << (opcode & 0x07)));
/*TODO*///	
/*TODO*///		if ((opcode & 0x07) == 0x05)
/*TODO*///		{
/*TODO*///			/* reset coin counter */
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUTbi_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((opcode & 0x07) != 6)
/*TODO*///			return opOUTsnd_A (opcode);
/*TODO*///	
/*TODO*///		vgColour = register_A & 0x01 ? 0x0f: 0x07;
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUT16_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((opcode & 0x07) != 6)
/*TODO*///			return opOUTsnd_A (opcode);
/*TODO*///	
/*TODO*///		if ((register_A & 0x1) != 1)
/*TODO*///		{
/*TODO*///			vgColour = FromX & 0x0F;
/*TODO*///	
/*TODO*///			if (!vgColour)
/*TODO*///				vgColour = 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUT64_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUTWW_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((opcode & 0x07) != 6)
/*TODO*///			return opOUTsnd_A (opcode);
/*TODO*///	
/*TODO*///		if ((register_A & 0x1) == 1)
/*TODO*///		{
/*TODO*///			CINEWORD temp_word = ~FromX & 0x0FFF;
/*TODO*///			if (!temp_word)   /* black */
/*TODO*///				vgColour = 0;
/*TODO*///			else
/*TODO*///			{   /* non-black */
/*TODO*///				if ((temp_word & 0x0888) != 0)
/*TODO*///					/* bright */
/*TODO*///					vgColour = ((temp_word >> 1) & 0x04) | ((temp_word >> 6) & 0x02) | ((temp_word >> 11) & 0x01) | 0x08;
/*TODO*///				else if ((temp_word & 0x0444) != 0)
/*TODO*///					/* dim bits */
/*TODO*///					vgColour = (temp_word & 0x04) | ((temp_word >> 5) & 0x02) | ((temp_word >> 10) & 0x01);
/*TODO*///			}
/*TODO*///		} /* colour change? == 1 */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUTsnd_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUTbi_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x07;
/*TODO*///	
/*TODO*///		if (temp_byte - 0x06)
/*TODO*///			return opOUTsnd_B (opcode);
/*TODO*///	
/*TODO*///		vgColour = ((register_B & 0x01) << 3) | 0x07;
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUT16_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x07;
/*TODO*///	
/*TODO*///		if (temp_byte - 0x06)
/*TODO*///			return opOUTsnd_B (opcode);
/*TODO*///	
/*TODO*///		if ((register_B & 0xFF) != 1)
/*TODO*///		{
/*TODO*///			vgColour = FromX & 0x0F;
/*TODO*///	
/*TODO*///			if (!vgColour)
/*TODO*///				vgColour = 1;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUT64_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opOUTWW_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_BB;
/*TODO*///	}
	
	  /* LDA imm (0x) */
	public static opcode_func opLDAimm_A_AA = new opcode_func() {
        @Override
            public int handler(int opcode) {
                CINEWORD temp_word = new CINEWORD(opcode & 0x0F);   /* pick up immediate value */
		temp_word.set( temp_word.get()<< 8);                          /* LDAimm is the HIGH nibble!*/
	
		cmp_new = temp_word;                      /* set new comparison flag */
	
		SETA0 (register_A.get());                     /* save old accA bit0 */
		SETFC (register_A.get());                     /* ??? clear carry? */
	
		cmp_old = register_A;                     /* step back cmp flag */
		register_A = temp_word;                   /* set the register */
	
		return state_AA;                           /* swap state and end opcode */
            }
        };
        
	
public static opcode_func opLDAimm_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = opcode & 0x0F;   /* pick up immediate value */
/*TODO*///		temp_word <<= 8;                          /* LDAimm is the HIGH nibble!*/
/*TODO*///	
/*TODO*///		cmp_new = temp_word;                      /* set new comparison flag */
/*TODO*///	
/*TODO*///		SETA0 (register_A);                     /* save old accA bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_B;                     /* step back cmp flag */
/*TODO*///		register_B = temp_word;                   /* set the register */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDAdir_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;        /* snag imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;  /* set I register */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                  /* new acc value */
/*TODO*///	
/*TODO*///		SETA0 (register_A);                          /* back up bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_A;                          /* store old acc */
/*TODO*///		register_A = cmp_new;                          /* store new acc */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDAdir_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;        /* snag imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;  /* set I register */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                  /* new acc value */
/*TODO*///	
/*TODO*///		SETA0 (register_A);                          /* back up bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_B;                          /* store old acc */
/*TODO*///		register_B = cmp_new;                          /* store new acc */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDAirg_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///	
/*TODO*///		SETA0 (register_A);
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_A;
/*TODO*///		register_A = cmp_new;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDAirg_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///	
/*TODO*///		SETA0 (register_A);
/*TODO*///		SETFC (register_A);
/*TODO*///	
/*TODO*///		cmp_old = register_B;
/*TODO*///		register_B = cmp_new;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
	
	  /* ADD imm */
	public static opcode_func opADDimm_A_AA  = new opcode_func() {
            @Override
            public int handler(int opcode) {
		CINEWORD temp_word = new CINEWORD(opcode & 0x0F);     /* get imm value */
	
		cmp_new = temp_word;                        /* new acc value */
		SETA0 (register_A.get());                       /* save old accA bit0 */
		cmp_old = register_A;                       /* store old acc for later */
	
		register_A.set( register_A.get() + temp_word.get());                    /* add values */
		SETFC (register_A.get());                       /* store carry and extra */
		register_A.set( register_A.get() & 0xFFF );                        /* toss out >12bit carry */
	
		return state_AA;
            }
        };
	
public static opcode_func opADDimm_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = opcode & 0x0F;     /* get imm value */
/*TODO*///	
/*TODO*///		cmp_new = temp_word;                        /* new acc value */
/*TODO*///		SETA0 (register_A);                       /* save old accA bit0 */
/*TODO*///		cmp_old = register_B;                       /* store old acc for later */
/*TODO*///	
/*TODO*///		register_B += temp_word;                    /* add values */
/*TODO*///		SETFC (register_B);                       /* store carry and extra */
/*TODO*///		register_B &= 0xFFF;                        /* toss out >12bit carry */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
	
	  /* ADD imm extended */
	public static opcode_func opADDimmX_A_AA  = new opcode_func() {
            @Override
            public int handler(int opcode) {
		cmp_new = new CINEWORD(CCPU_FETCH (register_PC.get()));       /* get extended value */
                register_PC.set(register_PC.get() + 1);
		SETA0 (register_A.get());                       /* save old accA bit0 */
		cmp_old = register_A;                       /* store old acc for later */
	
		register_A.set( register_A.get() + cmp_new.get() );                      /* add values */
		SETFC (register_A.get());                       /* store carry and extra */
		register_A.set( register_A.get() & 0xFFF );                        /* toss out >12bit carry */
	
		return state_AA;
            }
        };
	
public static opcode_func opADDimmX_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_FETCH (register_PC++);       /* get extended value */
/*TODO*///		SETA0 (register_A);                       /* save old accA bit0 */
/*TODO*///		cmp_old = register_B;                       /* store old acc for later */
/*TODO*///	
/*TODO*///		register_B += cmp_new;                      /* add values */
/*TODO*///		SETFC (register_B);                       /* store carry and extra */
/*TODO*///		register_B &= 0xFFF;                        /* toss out >12bit carry */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opADDdir_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;         /* fetch imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;   /* set regI addr */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                   /* fetch imm real value */
/*TODO*///		SETA0 (register_A);                           /* store bit0 */
/*TODO*///		cmp_old = register_A;                           /* store old acc value */
/*TODO*///	
/*TODO*///		register_A += cmp_new;                          /* do acc operation */
/*TODO*///		SETFC (register_A);                           /* store carry and extra */
/*TODO*///		register_A &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opADDdir_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;         /* fetch imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;   /* set regI addr */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                   /* fetch imm real value */
/*TODO*///		SETA0 (register_A);                           /* store bit0 */
/*TODO*///		cmp_old = register_B;                           /* store old acc value */
/*TODO*///	
/*TODO*///		register_B += cmp_new;                          /* do acc operation */
/*TODO*///		SETFC (register_B);                           /* store carry and extra */
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opAWDirg_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		register_A += cmp_new;
/*TODO*///		SETFC (register_A);
/*TODO*///		register_A &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opAWDirg_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		register_B += cmp_new;
/*TODO*///		SETFC (register_B);
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBimm_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * 	SUBtractions are negate-and-add instructions of the CCPU; what
/*TODO*///		 * 	a pain in the ass.
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = opcode & 0x0F;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word = (temp_word ^ 0xFFF) + 1;         /* ones compliment */
/*TODO*///		register_A += temp_word;                       /* add */
/*TODO*///		SETFC (register_A);                          /* pick up top bits */
/*TODO*///		register_A &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBimm_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * SUBtractions are negate-and-add instructions of the CCPU; what
/*TODO*///		 * a pain in the ass.
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = opcode & 0x0F;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word = (temp_word ^ 0xFFF) + 1;         /* ones compliment */
/*TODO*///		register_B += temp_word;                       /* add */
/*TODO*///		SETFC (register_B);                          /* pick up top bits */
/*TODO*///		register_B &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
	
	public static opcode_func opSUBimmX_A_AA  = new opcode_func() {
            @Override
            public int handler(int opcode) {
		CINEWORD temp_word = new CINEWORD(CCPU_FETCH (register_PC.get()));       /* snag imm value */
                register_PC.set(register_PC.get() + 1);
	
		cmp_new = temp_word;                          /* save cmp value */
		SETA0 (register_A.get());                         /* store bit0 */
		cmp_old = register_A;                         /* back up regA */
	
		temp_word.set((temp_word.get() ^ 0xFFF) + 1);        /* ones compliment */
		register_A.set( register_A.get() + temp_word.get() );                      /* add */
		SETFC (register_A.get());                         /* pick up top bits */
		register_A.set( register_A.get() & 0x0FFF);                         /* mask final regA value */
	
		return state_AA;
            }
        };
	
public static opcode_func opSUBimmX_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = CCPU_FETCH (register_PC++);       /* snag imm value */
/*TODO*///	
/*TODO*///		cmp_new = temp_word;                          /* save cmp value */
/*TODO*///		SETA0 (register_A);                         /* store bit0 */
/*TODO*///		cmp_old = register_B;                         /* back up regA */
/*TODO*///	
/*TODO*///		temp_word = (temp_word ^ 0xFFF) + 1;        /* ones compliment */
/*TODO*///		register_B += temp_word;                      /* add */
/*TODO*///		SETFC (register_B);                         /* pick up top bits */
/*TODO*///		register_B &= 0x0FFF;                         /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBdir_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = opcode & 0x0F;         /* fetch imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_word;   /* set regI addr */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word = (cmp_new ^ 0xFFF) + 1;           /* ones compliment */
/*TODO*///		register_A += temp_word;                       /* add */
/*TODO*///		SETFC (register_A);                          /* pick up top bits */
/*TODO*///		register_A &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBdir_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word;
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;         /* fetch imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;   /* set regI addr */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word = (cmp_new ^ 0xFFF) + 1;           /* ones compliment */
/*TODO*///		register_B += temp_word;                       /* add */
/*TODO*///		SETFC (register_B);                          /* pick up top bits */
/*TODO*///		register_B &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBirg_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word;
/*TODO*///	
/*TODO*///		/* sub [i] */
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word = (cmp_new ^ 0xFFF) + 1;           /* ones compliment */
/*TODO*///		register_A += temp_word;                       /* add */
/*TODO*///		SETFC (register_A);                          /* pick up top bits */
/*TODO*///		register_A &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSUBirg_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word;
/*TODO*///	
/*TODO*///		/* sub [i] */
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word = (cmp_new ^ 0xFFF) + 1;           /* ones compliment */
/*TODO*///		register_B += temp_word;                       /* add */
/*TODO*///		SETFC (register_B);                          /* pick up top bits */
/*TODO*///		register_B &= 0x0FFF;                          /* mask final regA value */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* CMP dir */
public static opcode_func opCMPdir_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * compare direct mode; don't modify regs, just set carry flag or not.
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CINEWORD temp_word;
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;       /* obtain relative addr */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte; /* build real addr */
/*TODO*///	
/*TODO*///		temp_word = CCPU_RAM_RDMEM(register_I);
/*TODO*///		cmp_new = temp_word;                          /* new acc value */
/*TODO*///		SETA0 (register_A);                         /* backup bit0 */
/*TODO*///		cmp_old = register_A;                         /* backup old acc */
/*TODO*///	
/*TODO*///		temp_word = (temp_word ^ 0xFFF) + 1;        /* ones compliment */
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);                          /* pick up top bits */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opCMPdir_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word;
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;       /* obtain relative addr */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte; /* build real addr */
/*TODO*///	
/*TODO*///		temp_word = CCPU_RAM_RDMEM(register_I);
/*TODO*///		cmp_new = temp_word;                          /* new acc value */
/*TODO*///		SETA0 (register_A);                         /* backup bit0 */
/*TODO*///		cmp_old = register_B;                         /* backup old acc */
/*TODO*///	
/*TODO*///		temp_word = (temp_word ^ 0xFFF) + 1;        /* ones compliment */
/*TODO*///		temp_word += register_B;
/*TODO*///		SETFC (temp_word);                          /* pick up top bits */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* AND [i] */
public static opcode_func opANDirg_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                /* new acc value */
/*TODO*///		SETA0 (register_A);
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		register_A &= cmp_new;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opANDirg_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_RAM_RDMEM(register_I);                /* new acc value */
/*TODO*///		SETA0 (register_A);
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		register_B &= cmp_new;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* LDJ imm */
public static opcode_func opLDJimm_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = CCPU_FETCH (register_PC++);      /* upper part of address */
/*TODO*///		temp_byte = (temp_byte << 4) |             /* Silly CCPU; Swap */
/*TODO*///		            (temp_byte >> 4);              /* nibbles */
/*TODO*///	
/*TODO*///		/* put the upper 8 bits above the existing 4 bits */
/*TODO*///		register_J = (opcode & 0x0F) | (temp_byte << 4);
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDJimm_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = CCPU_FETCH (register_PC++);      /* upper part of address */
/*TODO*///		temp_byte = (temp_byte << 4) |             /* Silly CCPU; Swap */
/*TODO*///		            (temp_byte >> 4);              /* nibbles */
/*TODO*///	
/*TODO*///		/* put the upper 8 bits above the existing 4 bits */
/*TODO*///		register_J = (opcode & 0x0F) | (temp_byte << 4);
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* LDJ irg */
public static opcode_func opLDJirg_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* load J reg from value at last dir addr */
/*TODO*///		register_J = CCPU_RAM_RDMEM(register_I);
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDJirg_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		register_J = CCPU_RAM_RDMEM(register_I);
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* LDP imm */
public static opcode_func opLDPimm_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* load page register from immediate */
/*TODO*///		register_P = opcode & 0x0F;  /* set page register */
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDPimm_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* load page register from immediate */
/*TODO*///		register_P = opcode & 0x0F;  /* set page register */
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* LDI dir */
public static opcode_func opLDIdir_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* load regI directly .. */
/*TODO*///	
/*TODO*///		CINEBYTE temp_byte = (register_P << 4) +           /* get ram page ... */
/*TODO*///		         (opcode & 0x0F); /* and imm half of ram addr.. */
/*TODO*///	
/*TODO*///		register_I = CCPU_RAM_RDMEM(temp_byte) & 0xFF;      /* set/mask new register_I */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLDIdir_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = (register_P << 4) +           /* get ram page ... */
/*TODO*///		         (opcode & 0x0F); /* and imm half of ram addr.. */
/*TODO*///	
/*TODO*///		register_I = CCPU_RAM_RDMEM(temp_byte) & 0xFF;      /* set/mask new register_I */
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* STA dir */
public static opcode_func opSTAdir_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;        /* snag imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;  /* set I register */
/*TODO*///	
/*TODO*///		CCPU_RAM_WRMEM(register_I, register_A);               /* store acc to RAM */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSTAdir_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0x0F;        /* snag imm value */
/*TODO*///	
/*TODO*///		register_I = (register_P << 4) + temp_byte;  /* set I register */
/*TODO*///	
/*TODO*///		CCPU_RAM_WRMEM(register_I, register_B);               /* store acc to RAM */
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* STA irg */
public static opcode_func opSTAirg_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * STA into address specified in regI. Nice and easy :)
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CCPU_RAM_WRMEM(register_I, register_A);               /* store acc */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opSTAirg_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CCPU_RAM_WRMEM(register_I, register_B);               /* store acc */
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* XLT */
public static opcode_func opXLT_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * XLT is weird; it loads the current accumulator with the bytevalue
/*TODO*///		 * at ROM location pointed to by the accumulator; this allows the
/*TODO*///		 * program to read the program itself..
/*TODO*///		 * 		NOTE! Next opcode is *IGNORED!* because of a twisted side-effect
/*TODO*///		 */
/*TODO*///	
/*TODO*///		cmp_new = CCPU_FETCH (((register_PC - 1) & 0xF000) + register_A);   /* store new acc value */
/*TODO*///		SETA0 (register_A);           /* store bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_A;           /* back up acc */
/*TODO*///	
/*TODO*///		register_A = cmp_new;           /* new acc value */
/*TODO*///	
/*TODO*///		register_PC++;               /* bump PC twice because XLT is fucked up */
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opXLT_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = CCPU_FETCH (((register_PC - 1) & 0xF000) + register_B);   /* store new acc value */
/*TODO*///		SETA0 (register_A);           /* store bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_B;           /* back up acc */
/*TODO*///	
/*TODO*///		register_B = cmp_new;           /* new acc value */
/*TODO*///	
/*TODO*///		register_PC++;               /* bump PC twice because XLT is fucked up */
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* MUL [i] */
public static opcode_func opMULirg_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = opcode & 0xFF;    /* (for ease and speed) */
/*TODO*///		CINEWORD temp_word = CCPU_RAM_RDMEM(register_I);               /* pick up ram value */
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///	
/*TODO*///		temp_word <<= 4;                              /* shift into ADD position */
/*TODO*///		register_B <<= 4;                             /* get sign bit 15 */
/*TODO*///		register_B |= (register_A >> 8);            /* bring in A high nibble */
/*TODO*///	
/*TODO*///		register_A = ((register_A & 0xFF) << 8) | /* shift over 8 bits */
/*TODO*///		          temp_byte;  /* pick up opcode */
/*TODO*///	
/*TODO*///		if ((register_A & 0x100) != 0)
/*TODO*///		{        				   /* 1bit shifted out? */
/*TODO*///			register_A = (register_A >> 8) |
/*TODO*///			             ((register_B & 0xFF) << 8);
/*TODO*///	
/*TODO*///			SETA0 (register_A & 0xFF);                  /* store bit0 */
/*TODO*///	
/*TODO*///			register_A >>= 1;
/*TODO*///			register_A &= 0xFFF;
/*TODO*///	
/*TODO*///			register_B = SAR(register_B,4);
/*TODO*///			cmp_old = register_B & 0x0F;
/*TODO*///	
/*TODO*///			register_B = SAR(register_B,1);
/*TODO*///	
/*TODO*///			register_B &= 0xFFF;
/*TODO*///			register_B += cmp_new;
/*TODO*///	
/*TODO*///			SETFC (register_B);
/*TODO*///	
/*TODO*///			register_B &= 0xFFF;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			register_A = (register_A >> 8) |    /* Bhigh | Alow */
/*TODO*///			             ((register_B & 0xFF) << 8);
/*TODO*///	
/*TODO*///			temp_word = register_A & 0xFFF;
/*TODO*///	
/*TODO*///			SETA0 (temp_word & 0xFF);                   /* store bit0 */
/*TODO*///			cmp_old = temp_word;
/*TODO*///	
/*TODO*///			temp_word += cmp_new;
/*TODO*///			SETFC (temp_word);
/*TODO*///	
/*TODO*///			register_A >>= 1;
/*TODO*///			register_A &= 0xFFF;
/*TODO*///	
/*TODO*///			register_B = SAR(register_B,5);
/*TODO*///			register_B &= 0xFFF;
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opMULirg_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = CCPU_RAM_RDMEM(register_I);
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		cmp_old = register_B;
/*TODO*///		SETA0 (register_A & 0xFF);
/*TODO*///	
/*TODO*///		register_B <<= 4;
/*TODO*///	
/*TODO*///		register_B = SAR(register_B,5);
/*TODO*///	
/*TODO*///		if ((register_A & 0x01) != 0)
/*TODO*///		{
/*TODO*///			register_B += temp_word;
/*TODO*///			SETFC (register_B);
/*TODO*///			register_B &= 0x0FFF;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			temp_word += register_B;
/*TODO*///			SETFC (temp_word);
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* LSRe */
public static opcode_func opLSRe_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * EB; right shift pure; fill new bit with zero.
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = 0x0BEB;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_A >>= 1;
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSRe_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = 0x0BEB;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word += register_B;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_B >>= 1;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSRf_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLSRf 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSRf_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLSRf 2\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLe_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * EC; left shift pure; fill new bit with zero *
/*TODO*///		 */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = 0x0CEC;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_A <<= 1;
/*TODO*///		register_A &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLe_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = 0x0CEC;                          /* data register */
/*TODO*///	
/*TODO*///		cmp_new = temp_word;                         /* magic value */
/*TODO*///		SETA0 (register_A);                        /* back up bit0 */
/*TODO*///		cmp_old = register_B;                        /* store old acc */
/*TODO*///	
/*TODO*///		temp_word += register_B;                     /* add to acc */
/*TODO*///		SETFC (temp_word);                         /* store carry flag */
/*TODO*///		register_B <<= 1;                            /* add regA to itself */
/*TODO*///		register_B &= 0xFFF;                         /* toss excess bits */
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLf_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLSLf 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLf_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLSLf 2\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRe_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* agh! I dislike these silly 12bit processors :P */
/*TODO*///	
/*TODO*///		cmp_new = 0x0DED;
/*TODO*///		SETA0 (register_A);           /* store bit0 */
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		register_A <<= 4; /* get sign bit */
/*TODO*///		register_A = SAR(register_A,5);
/*TODO*///		register_A &= 0xFFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRe_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		cmp_new = 0x0DED;
/*TODO*///		SETA0 (register_A);
/*TODO*///		SETFC (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		register_B <<= 4;
/*TODO*///		register_B = SAR(register_B,5);
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRf_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opASRf 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRf_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opASRf 2\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRDe_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * Arithmetic shift right of D (A+B) .. B is high (sign bits).
/*TODO*///		 * divide by 2, but leave the sign bit the same. (ie: 1010 . 1001)
/*TODO*///		 */
/*TODO*///		CINEWORD temp_word = 0x0EEE;
/*TODO*///		CINEWORD temp_word_2;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;          /* save new acc value */
/*TODO*///		SETA0 (register_A & 0xFF);  /* save old accA bit0 */
/*TODO*///		cmp_old = register_A;         /* save old acc */
/*TODO*///	
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_A <<= 4;
/*TODO*///		register_B <<= 4;
/*TODO*///	
/*TODO*///		temp_word_2 = (register_B >> 4) << 15;
/*TODO*///		register_B = SAR(register_B,5);
/*TODO*///		register_A = (register_A >> 1) | temp_word_2;
/*TODO*///		register_A >>= 4;
/*TODO*///	
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRDe_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = 0x0EEE;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A & 0xFF);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word += register_B;
/*TODO*///		SETFC (temp_word);
/*TODO*///		register_B <<= 4;
/*TODO*///		register_B = SAR(register_B,5);
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRDf_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opASRDf 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opASRDf_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opASRDf 2\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLDe_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* LSLDe -- Left shift through both accumulators; lossy in middle. */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = 0x0FEF;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);
/*TODO*///		register_A <<= 1;                             /* logical shift left */
/*TODO*///		register_A &= 0xFFF;
/*TODO*///	
/*TODO*///		register_B <<= 1;
/*TODO*///		register_B &= 0xFFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLDe_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLSLD 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLDf_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* LSLDf */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = 0x0FFF;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_A;
/*TODO*///	
/*TODO*///		temp_word += register_A;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_A <<= 1;
/*TODO*///		register_A &= 0x0FFF;
/*TODO*///	
/*TODO*///		register_B <<= 1;
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLSLDf_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* not 'the same' as the A.AA version above */
/*TODO*///	
/*TODO*///		CINEWORD temp_word = 0x0FFF;
/*TODO*///	
/*TODO*///		cmp_new = temp_word;
/*TODO*///		SETA0 (register_A);
/*TODO*///		cmp_old = register_B;
/*TODO*///	
/*TODO*///		temp_word += register_B;
/*TODO*///		SETFC (temp_word);
/*TODO*///	
/*TODO*///		register_B <<= 1;
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///	
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMP_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * simple jump; change PC and continue..
/*TODO*///		 */
/*TODO*///	
/*TODO*///		JMP();
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMP_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		JMP();
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEI_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((FromX & 0x800) != 0)
/*TODO*///			FromX |= 0xF000;
/*TODO*///		if (!(CCPU_READPORT (CCPU_PORT_IOOUTPUTS) & 0x80))
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKY) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKX) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEI_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((FromX & 0x800) != 0)
/*TODO*///			FromX |= 0xF000;
/*TODO*///		if (!(CCPU_READPORT (CCPU_PORT_IOOUTPUTS) & 0x80))
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKY) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKX) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEI_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((FromX & 0x800) != 0)
/*TODO*///			FromX |= 0xF000;
/*TODO*///		if (!(CCPU_READPORT (CCPU_PORT_IOOUTPUTS) & 0x80))
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKY) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			if ((CCPU_READPORT (CCPU_PORT_IN_JOYSTICKX) - (CINESWORD)FromX) < 0x800)
/*TODO*///				JMP();
/*TODO*///		}
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * previous instruction was not an ACC instruction, nor was the
/*TODO*///		 * instruction twice back a USB, therefore minus flag test the
/*TODO*///		 * current A-reg
/*TODO*///		 */
/*TODO*///	
/*TODO*///		/* negative acc? */
/*TODO*///		if ((register_A & 0x800) != 0)
/*TODO*///			JMP();	  /* yes -- do jump */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_AA_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* previous acc negative? Jump if so... */
/*TODO*///		if ((cmp_old & 0x800) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_BB_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((register_B & 0x800) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((register_A & 0x800) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJLT_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* jump if old acc equals new acc */
/*TODO*///	
/*TODO*///		if (cmp_new < cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJLT_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (cmp_new < cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEQ_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* jump if equal */
/*TODO*///	
/*TODO*///		if (cmp_new == cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEQ_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (cmp_new == cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJA0_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((acc_a0 & 0x01) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJA0_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((acc_a0 & 0x01) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJNC_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (!(GETFC() & 0xF0))
/*TODO*///			JMP(); /* no carry, so jump */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJNC_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (!(GETFC() & 0xF0))
/*TODO*///			JMP(); /* no carry, so jump */
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJDR_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * Calculate number of cycles executed since
/*TODO*///		 * last 'VDR' instruction, add two and use as
/*TODO*///		 * cycle count, never branch
/*TODO*///		 */
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJDR_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * Calculate number of cycles executed since
/*TODO*///		 * last 'VDR' instruction, add two and use as
/*TODO*///		 * cycle count, never branch
/*TODO*///		 */
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opNOP_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opNOP_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP32_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * 00 = Offset 0000h
/*TODO*///		 * 01 = Offset 1000h
/*TODO*///		 * 02 = Offset 2000h
/*TODO*///		 * 03 = Offset 3000h
/*TODO*///		 * 04 = Offset 4000h
/*TODO*///		 * 05 = Offset 5000h
/*TODO*///		 * 06 = Offset 6000h
/*TODO*///		 * 07 = Offset 7000h
/*TODO*///		 */
/*TODO*///		CINEWORD temp_word = (register_P & 0x07) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP32_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = (register_P & 0x07) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP16_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * 00 = Offset 0000h
/*TODO*///		 * 01 = Offset 1000h
/*TODO*///		 * 02 = Offset 2000h
/*TODO*///		 * 03 = Offset 3000h
/*TODO*///		 */
/*TODO*///		CINEWORD temp_word = (register_P & 0x03) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP16_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = (register_P & 0x03) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMP_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		JMP();
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP8_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/*
/*TODO*///		 * "long jump"; combine P and J to jump to a new far location (that can
/*TODO*///		 * 	be more than 12 bits in address). After this jump, further jumps
/*TODO*///		 * are local to this new page.
/*TODO*///		 */
/*TODO*///		CINEWORD temp_word = ((register_P & 0x03) - 1) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJPP8_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEWORD temp_word = ((register_P & 0x03) - 1) << 12;  /* rom offset */
/*TODO*///		register_PC = register_J + temp_word;
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if ((register_A & 0x800) != 0)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_AA_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opJMI 3\n");
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJMI_BB_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opJMI 4\n");
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJLT_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (cmp_new < cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJEQ_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (cmp_new == cmp_old)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJA0_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (GETA0() & 0x01)
/*TODO*///			JMP();
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJNC_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		if (!(GETFC() & 0x0F0))
/*TODO*///			JMP(); /* if no carry, jump */
/*TODO*///	
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opJDR_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* register_PC++; */
/*TODO*///		logerror("The hell? No PC incrementing?\n");
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
/*TODO*///	  /* NOP */
public static opcode_func opNOP_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return state_B;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLLT_A_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		CINEBYTE temp_byte = 0;
/*TODO*///	
/*TODO*///		while (1)
/*TODO*///		{
/*TODO*///			CINEWORD temp_word = register_A >> 8;  /* register_A's high bits */
/*TODO*///			temp_word &= 0x0A;                   /* only want PA11 and PA9 */
/*TODO*///	
/*TODO*///			if (temp_word != 0)
/*TODO*///			{
/*TODO*///				temp_word ^= 0x0A;                   /* flip the bits */
/*TODO*///	
/*TODO*///				if (temp_word != 0)
/*TODO*///					break;                	        /* if not zero, mismatch found */
/*TODO*///			}
/*TODO*///	
/*TODO*///			temp_word = register_B >> 8;         /* regB's top bits */
/*TODO*///			temp_word &= 0x0A;                   /* only want SA11 and SA9 */
/*TODO*///	
/*TODO*///			if (temp_word != 0)
/*TODO*///			{
/*TODO*///				temp_word ^= 0x0A;                   /* flip bits */
/*TODO*///	
/*TODO*///				if (temp_word != 0)
/*TODO*///					break;                          /* if not zero, mismatch found */
/*TODO*///			}
/*TODO*///	
/*TODO*///			register_A <<= 1;                    /* shift regA */
/*TODO*///			register_B <<= 1;                    /* shift regB */
/*TODO*///	
/*TODO*///			temp_byte ++;
/*TODO*///			if (!temp_byte)
/*TODO*///				return state_AA;
/*TODO*///	        /* try again */
/*TODO*///		}
/*TODO*///	
/*TODO*///		vgShiftLength = temp_byte;
/*TODO*///		register_A &= 0x0FFF;
/*TODO*///		register_B &= 0x0FFF;
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opLLT_B_AA  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opLLT 1\n");
/*TODO*///		return state_AA;
/*TODO*///	}
/*TODO*///	
public static opcode_func opVIN_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* set the starting address of a vector */
/*TODO*///	
/*TODO*///		FromX = register_A & 0xFFF;            /* regA goes to x-coord */
/*TODO*///		FromY = register_B & 0xFFF;            /* regB goes to y-coord */
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opVIN_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		FromX = register_A & 0xFFF;            /* regA goes to x-coord */
/*TODO*///		FromY = register_B & 0xFFF;            /* regB goes to y-coord */
/*TODO*///	
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
public static opcode_func opWAI_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* wait for a tick on the watchdog */
/*TODO*///		bNewFrame = 1;
/*TODO*///		bailOut = TRUE;
/*TODO*///		return state;
/*TODO*///	}
/*TODO*///	
public static opcode_func opWAI_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		bNewFrame = 1;
/*TODO*///		bailOut = TRUE;
/*TODO*///		return state;
/*TODO*///	}
/*TODO*///	
public static opcode_func opVDR_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* set ending points and draw the vector, or buffer for a later draw. */
/*TODO*///		int ToX = register_A & 0xFFF;
/*TODO*///		int ToY = register_B & 0xFFF;
/*TODO*///	
/*TODO*///		/*
/*TODO*///		 * shl 20, sar 20; this means that if the CCPU reg should be -ve,
/*TODO*///		 * we should be negative as well.. sign extended.
/*TODO*///		 */
/*TODO*///		if ((FromX & 0x800) != 0)
/*TODO*///			FromX |= 0xFFFFF000;
/*TODO*///		if ((ToX & 0x800) != 0)
/*TODO*///			ToX |= 0xFFFFF000;
/*TODO*///		if ((FromY & 0x800) != 0)
/*TODO*///			FromY |= 0xFFFFF000;
/*TODO*///		if ((ToY & 0x800) != 0)
/*TODO*///			ToY |= 0xFFFFF000;
/*TODO*///	
/*TODO*///		/* figure out the vector */
/*TODO*///		ToX -= FromX;
/*TODO*///		ToX = SAR(ToX,vgShiftLength);
/*TODO*///		ToX += FromX;
/*TODO*///	
/*TODO*///		ToY -= FromY;
/*TODO*///		ToY = SAR(ToY,vgShiftLength);
/*TODO*///		ToY += FromY;
/*TODO*///	
/*TODO*///		/* do orientation flipping, etc. */
/*TODO*///		/* NOTE: this has been removed on the assumption that the vector draw routine can do it all */
/*TODO*///	#if !RAW_VECTORS
/*TODO*///		if (bFlipX != 0)
/*TODO*///		{
/*TODO*///			ToX = sdwGameXSize - ToX;
/*TODO*///			FromX = sdwGameXSize - FromX;
/*TODO*///		}
/*TODO*///	
/*TODO*///		if (bFlipY != 0)
/*TODO*///		{
/*TODO*///			ToY = sdwGameYSize - ToY;
/*TODO*///			FromY = sdwGameYSize - FromY;
/*TODO*///		}
/*TODO*///	
/*TODO*///		FromX += sdwXOffset;
/*TODO*///		ToX += sdwXOffset;
/*TODO*///	
/*TODO*///		FromY += sdwYOffset;
/*TODO*///		ToY += sdwYOffset;
/*TODO*///	
/*TODO*///		/* check real coords */
/*TODO*///		if (bSwapXY != 0)
/*TODO*///		{
/*TODO*///			CINEWORD temp_word;
/*TODO*///	
/*TODO*///			temp_word = ToY;
/*TODO*///			ToY = ToX;
/*TODO*///			ToX = temp_word;
/*TODO*///	
/*TODO*///			temp_word = FromY;
/*TODO*///			FromY = FromX;
/*TODO*///			FromX = temp_word;
/*TODO*///		}
/*TODO*///	#endif
/*TODO*///	
/*TODO*///		/* render the line */
/*TODO*///		CinemaVectorData (FromX, FromY, ToX, ToY, vgColour);
/*TODO*///	
/*TODO*///		return state_A;
/*TODO*///	}
/*TODO*///	
public static opcode_func opVDR_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		UNFINISHED ("opVDR B 1\n");
/*TODO*///		return state_BB;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * some code needs to be changed based on the machine or switches set.
/*TODO*///	 * Instead of getting disorganized, I'll put the extra dispatchers
/*TODO*///	 * here. The main dispatch loop jumps here, checks options, and
/*TODO*///	 * redispatches to the actual opcode handlers.
/*TODO*///	 */
/*TODO*///	
/*TODO*///	/* JPP series of opcodes */
public static opcode_func tJPP_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* MSIZE -- 0 = 4k, 1 = 8k, 2 = 16k, 3 = 32k */
/*TODO*///		switch (ccpu_msize)
/*TODO*///		{
/*TODO*///			case CCPU_MEMSIZE_4K:
/*TODO*///			case CCPU_MEMSIZE_8K:
/*TODO*///				return opJPP8_A_B (opcode);
/*TODO*///			case CCPU_MEMSIZE_16K:
/*TODO*///				return opJPP16_A_B (opcode);
/*TODO*///			case CCPU_MEMSIZE_32K:
/*TODO*///				return opJPP32_A_B (opcode);
/*TODO*///		}
/*TODO*///		logerror("Out of range JPP!\n");
/*TODO*///		return opJPP32_A_B (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJPP_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		/* MSIZE -- 0 = 4k, 1 = 8k, 2 = 16k, 3 = 32k */
/*TODO*///		switch (ccpu_msize)
/*TODO*///		{
/*TODO*///			case CCPU_MEMSIZE_4K:
/*TODO*///			case CCPU_MEMSIZE_8K:
/*TODO*///				return opJPP8_B_BB (opcode);
/*TODO*///			case CCPU_MEMSIZE_16K:
/*TODO*///				return opJPP16_B_BB (opcode);
/*TODO*///			case CCPU_MEMSIZE_32K:
/*TODO*///				return opJPP32_B_BB (opcode);
/*TODO*///		}
/*TODO*///		logerror("Out of range JPP!\n");
/*TODO*///		return state;
/*TODO*///	}
/*TODO*///	
/*TODO*///	/* JMI series of opcodes */
/*TODO*///	
public static opcode_func tJMI_A_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_A_B (opcode) : opJEI_A_B (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_A_A (opcode) : opJEI_A_A (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_AA_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_AA_B (opcode) : opJEI_AA_B (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_AA_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_AA_A (opcode) : opJEI_A_A (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_B_BB1  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_B_BB (opcode) : opJEI_B_BB (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_BB_B  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_BB_B (opcode) : opJEI_A_B (opcode);
/*TODO*///	}
/*TODO*///	
public static opcode_func tJMI_BB_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		return (ccpu_jmi_dip) ? opJMI_BB_A (opcode) : opJEI_A_A (opcode);
/*TODO*///	}
/*TODO*///	
/*TODO*///	/*
/*TODO*///	 * OUT series of opcodes:
/*TODO*///	 * ccpu_monitor can be one of:
/*TODO*///	 * 1 -- 16-level colour
/*TODO*///	 * 2 -- 64-level colour
/*TODO*///	 * 3 -- War of the Worlds colour
/*TODO*///	 * other -- bi-level
/*TODO*///	 */
public static opcode_func tOUT_A_A  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		switch (ccpu_monitor)
/*TODO*///		{
/*TODO*///			case CCPU_MONITOR_16LEV:
/*TODO*///				return opOUT16_A_A (opcode);
/*TODO*///			case CCPU_MONITOR_64LEV:
/*TODO*///				return opOUT64_A_A (opcode);
/*TODO*///			case CCPU_MONITOR_WOWCOL:
/*TODO*///				return opOUTWW_A_A (opcode);
/*TODO*///			default:
/*TODO*///				return opOUTbi_A_A (opcode);
/*TODO*///		}
/*TODO*///	}
/*TODO*///	
public static opcode_func tOUT_B_BB  = new opcode_func() { @Override public int handler(int opcode) {  throw new UnsupportedOperationException("Not supported yet."); }};
/*TODO*///	{
/*TODO*///		switch (ccpu_monitor)
/*TODO*///		{
/*TODO*///			case CCPU_MONITOR_16LEV:
/*TODO*///				return opOUT16_B_BB (opcode);
/*TODO*///			case CCPU_MONITOR_64LEV:
/*TODO*///				return opOUT64_B_BB (opcode);
/*TODO*///			case CCPU_MONITOR_WOWCOL:
/*TODO*///				return opOUTWW_B_BB (opcode);
/*TODO*///			default:
/*TODO*///				return opOUTbi_B_BB (opcode);
/*TODO*///		}
/*TODO*///	}
	
	/* Reset C-CPU registers, flags, etc to default starting values
	 */
	public void cineReset()
	{
		/* zero registers */
		register_PC = new CINEWORD(0);
		register_A = new CINEWORD(0);
		register_B = new CINEWORD(0);
		register_I = new CINEWORD(0);
		register_J = new CINEWORD(0);
		register_P = new CINEBYTE(0);
		FromX = new CINEWORD(0);
		FromY = new CINEWORD(0);
		register_T = new CINEWORD(0);
	
		/* zero flags */
		flag_C = new CINEWORD(0);
	
		/* reset state */
		state = state_A;
	
		/* reset internal state */
		cmp_old = new CINEWORD(0);
		cmp_new = new CINEWORD(0);
		SETA0 (0);
	}
	
	public static void cineSetJMI(int j)
	{
		ccpu_jmi_dip = j;
	/*
		if (ccpu_jmi_dip != 0)
			fprintf (stderr, "CCPU JMI Set: Yes.\n");
		else
			fprintf (stderr, "CCPU JMI Set: No.\n");
	*/
	}
	
	public static void cineSetMSize(int m)
	{
		ccpu_msize = m;
	/*
		switch (m)
		{
			case 0:
				fprintf (stderr, "CCPU Address Space: 4k\n");
				break;
			case 1:
				fprintf (stderr, "CCPU Address Space: 8k\n");
				break;
			case 2:
				fprintf (stderr, "CCPU Address Space: 16k\n");
				break;
			case 3:
				fprintf (stderr, "CCPU Address Space: 32k\n");
				break;
			default:
				fprintf (stderr, "CCPU Address Space: Error\n");
				break;
		}
	*/
	}
	
	public static void cineSetMonitor(int m)
	{
		ccpu_monitor = m;
	/*
		switch (m)
		{
			case 1:
				fprintf (stderr, "CCPU Monitor: 16-colour\n");
				break;
			case 2:
				fprintf (stderr, "CCPU Monitor: 64-colour\n");
				break;
			case 3:
				fprintf (stderr, "CCPU Monitor: War-of-the-Worlds-colour\n");
				break;
			default:
				fprintf (stderr, "CCPU Monitor: bi-level-display\n");
				break;
		}
	*/
	}
	
/*TODO*///	void cSetContext(CONTEXTCCPU *c)
/*TODO*///	{
/*TODO*///		cmp_old = c . accVal;
/*TODO*///		cmp_new = c . cmpVal;
/*TODO*///		SETA0 (c . pa0);
/*TODO*///		flag_C = c . cFlag;
/*TODO*///		register_PC = c . eRegPC;
/*TODO*///		register_A = c . eRegA;
/*TODO*///		register_B = c . eRegB;
/*TODO*///		register_I = c . eRegI;
/*TODO*///		register_J = c . eRegJ;
/*TODO*///		register_P = c . eRegP;
/*TODO*///		state = (CINESTATE)c . eCState;
/*TODO*///	}
	
	public void cGetContext(CONTEXTCCPU c)
	{
		c . accVal = cmp_old;
		c . cmpVal = cmp_new;
		c . pa0 = GETA0();
		c . cFlag = GETFC();
		c . eRegPC = register_PC;
		c . eRegA = register_A;
		c . eRegB = register_B;
		c . eRegI = register_I;
		c . eRegJ = register_J;
		c . eRegP = register_P;
		c . eCState = state;
	}

	        
}
