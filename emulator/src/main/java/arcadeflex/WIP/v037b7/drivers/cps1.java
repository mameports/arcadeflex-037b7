/***************************************************************************

  Capcom System 1
  ===============

  Driver provided by:
  Paul Leaman (paul@vortexcomputing.demon.co.uk)

  68000 for game, Z80, YM-2151 and OKIM6295 for sound.

  68000 clock speeds are unknown for all games (except where commented)

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package arcadeflex.WIP.v037b7.drivers;

import arcadeflex.common.ptrLib.UBytePtr;
import static arcadeflex.v037b7.generic.funcPtr.*;
import static arcadeflex.v037b7.mame.common.*;
import static arcadeflex.v037b7.mame.commonH.*;
import static arcadeflex.v037b7.mame.cpuintrf.*;
import static arcadeflex.v037b7.mame.cpuintrfH.*;        
import static arcadeflex.v037b7.mame.driverH.*;
import static arcadeflex.v037b7.mame.inptport.*;
import static arcadeflex.v037b7.mame.inptportH.*;
import static arcadeflex.v037b7.mame.input.*;
import static arcadeflex.v037b7.mame.inputH.*;
import static arcadeflex.v037b7.mame.memory.*;
import static arcadeflex.v037b7.mame.memoryH.*;
import static gr.codebb.arcadeflex.old.arcadeflex.osdepend.logerror;
import static arcadeflex.v037b7.machine.eeprom.*;
import static arcadeflex.v037b7.machine.eepromH.*; 
import static arcadeflex.WIP.v037b7.vidhrdw.cps1.*;
import static arcadeflex.v037b7.mame.drawgfxH.*;
import static arcadeflex.v037b7.mame.sndintrf.*;
import static arcadeflex.v037b7.mame.sndintrfH.*;
import static arcadeflex.v037b7.mame.usrintrf.usrintf_showmessage;        
import static arcadeflex.v037b7.sound._2151intf.*;
import static arcadeflex.v037b7.sound._2151intfH.*;
import static arcadeflex.v037b7.sound.mixerH.*;
import static arcadeflex.v037b7.sound.okim6295.*;
import arcadeflex.v037b7.sound.okim6295H.OKIM6295interface;
import static arcadeflex.v037b7.sound.qsound.*;
import static arcadeflex.v037b7.sound.qsoundH.*;        

public class cps1
{
	
	
	public static ReadHandlerPtr cps1_input2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int buttons=readinputport(5);
		return buttons << 8 | buttons;
	} };
	
	public static ReadHandlerPtr cps1_input3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int buttons=readinputport(6);
		return buttons << 8 | buttons;
	} };
	
	
	static int cps1_sound_fade_timer;
	
	public static WriteHandlerPtr cps1_snd_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU2);
		int length = memory_region_length(REGION_CPU2) - 0x10000;
		int bankaddr;
	
		bankaddr = (data * 0x4000) & (length-1);
		cpu_setbank(1,new UBytePtr(RAM, 0x10000 + bankaddr));
	
		if ((data & 0xfe) != 0) logerror("%04x: write %02x to f004n",cpu_get_pc(),data);
	} };
	
	public static WriteHandlerPtr cps1_sound_fade_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		cps1_sound_fade_timer=data;
	} };
	
	public static ReadHandlerPtr cps1_snd_fade_timer_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return cps1_sound_fade_timer;
	} };
	
	public static ReadHandlerPtr cps1_input_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int control=readinputport (offset/2);
		return (control<<8) | control;
	} };
	
	static int[] dial = new int[2];
/*TODO*///	
	public static ReadHandlerPtr forgottn_dial_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ((readinputport(5) - dial[0]) >> (4*offset)) & 0xff;
	} };
	
	public static ReadHandlerPtr forgottn_dial_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return ((readinputport(6) - dial[1]) >> (4*offset)) & 0xff;
	} };
	
	public static WriteHandlerPtr forgottn_dial_0_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dial[0] = readinputport(5);
	} };
	
	public static WriteHandlerPtr forgottn_dial_1_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		dial[1] = readinputport(6);
	} };
	
	public static WriteHandlerPtr cps1_coinctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	usrintf_showmessage("coinctrl %04x",data);
	
		if ((data & 0xff000000) == 0)
		{
			coin_lockout_w.handler(0,~data & 0x0400);
			coin_lockout_w.handler(1,~data & 0x0800);
			coin_counter_w.handler(0,data & 0x0100);
			coin_counter_w.handler(1,data & 0x0200);
		}
	
		if ((data & 0x00ff0000) == 0)
		{
/*TODO*///			/* mercs sets bit 0 */
/*TODO*///			set_led_status(0,data & 0x02);
/*TODO*///			set_led_status(1,data & 0x04);
/*TODO*///			set_led_status(2,data & 0x08);
		}
	} };
	
	public static WriteHandlerPtr cpsq_coinctrl2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((data & 0xff000000) == 0)
		{
			coin_lockout_w.handler(2,~data & 0x0002);
			coin_lockout_w.handler(3,~data & 0x0008);
			coin_counter_w.handler(2,data & 0x0001);
			coin_counter_w.handler(3,data & 0x0004);
	/*
	  	{
	       char baf[40];
	       sprintf(baf,"0xf1c004=%04x", data);
	       usrintf_showmessage(baf);
	       }
	*/
	    }
	} };
	
	public static InterruptPtr cps1_interrupt = new InterruptPtr() { public int handler() 
	{
		/* Strider also has a IRQ4 handler. It is input port related, but the game */
		/* works without it (maybe it's used to multiplex controls). It is the */
		/* *only* game to have that. */
		return 2;
	} };
	
	/********************************************************************
	*
	*  Q Sound
	*  =======
	*
	********************************************************************/
	
	static QSound_interface qsound_interface = new QSound_interface
	(
		QSOUND_CLOCK,
		REGION_SOUND1,
		new int[]{ 100,100 }
	);

	public static UBytePtr qsound_sharedram1=new UBytePtr(), qsound_sharedram2=new UBytePtr();

	public static InterruptPtr cps1_qsound_interrupt = new InterruptPtr() { public int handler() 
	{
/*TODO*///	#if 0
/*TODO*///	I have removed CPU_AUDIO_CPU from the Z(0 so this is no longer necessary
/*TODO*///		/* kludge to pass the sound board test with sound disabled */
/*TODO*///		if (Machine.sample_rate == 0)
/*TODO*///			qsound_sharedram1[0xfff] = 0x77;
/*TODO*///	#endif
	
		return 2;
	} };
	
	
	public static ReadHandlerPtr qsound_rom_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr rom = memory_region(REGION_USER1);
	
		if (rom != null) return rom.read(offset / 2) | 0xff00;
		else
		{
			usrintf_showmessage("%06x: read sound ROM byte %04x",cpu_get_pc(),offset/2);
			return 0;
		}
	} };
	
	public static ReadHandlerPtr qsound_sharedram1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return qsound_sharedram1.read(offset / 2) | 0xff00;
	} };
	
	public static WriteHandlerPtr qsound_sharedram1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		qsound_sharedram1.write(offset / 2, data);
	} };
	
	public static ReadHandlerPtr qsound_sharedram2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return qsound_sharedram2.read(offset / 2) | 0xff00;
	} };
	
	public static WriteHandlerPtr qsound_sharedram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		qsound_sharedram2.write(offset / 2, data);
	} };
	
	public static WriteHandlerPtr qsound_banksw_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*
		Z80 bank register for music note data. It's odd that it isn't encrypted
		though.
		*/
		UBytePtr RAM = memory_region(REGION_CPU2);
		int bankaddress=0x10000+((data&0x0f)*0x4000);
		if (bankaddress >= memory_region_length(REGION_CPU2))
		{
			logerror("WARNING: Q sound bank overflow (%02x)n", data);
			bankaddress=0x10000;
		}
		cpu_setbank(1, new UBytePtr(RAM, bankaddress));
	} };
	
	
	/********************************************************************
	*
	*  EEPROM
	*  ======
	*
	*   The EEPROM is accessed by a serial protocol using the register
	*   0xf1c006
	*
	********************************************************************/
	
	static EEPROM_interface qsound_eeprom_interface = new EEPROM_interface
	(
		7,		/* address bits */
		8,		/* data bits */
		"0110",	/*  read command */
		"0101",	/* write command */
		"0111"	/* erase command */
	);
	
/*TODO*///	static EEPROM_interface pang3_eeprom_interface = new EEPROM_interface
/*TODO*///	(
/*TODO*///		6,		/* address bits */
/*TODO*///		16,		/* data bits */
/*TODO*///		"0110",	/*  read command */
/*TODO*///		"0101",	/* write command */
/*TODO*///		"0111"	/* erase command */
/*TODO*///	);
	
	public static nvramPtr qsound_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
	{
		if (read_or_write != 0)
			EEPROM_save(file);
		else
		{
			EEPROM_init(qsound_eeprom_interface);
	
			if (file != null)
				EEPROM_load(file);
		}
	} };
	
/*TODO*///	public static nvramPtr pang3_nvram_handler  = new nvramPtr() { public void handler(Object file, int read_or_write) 
/*TODO*///	{
/*TODO*///		if (read_or_write != 0)
/*TODO*///			EEPROM_save(file);
/*TODO*///		else
/*TODO*///		{
/*TODO*///			EEPROM_init(&pang3_eeprom_interface);
/*TODO*///	
/*TODO*///			if (file != 0)
/*TODO*///				EEPROM_load(file);
/*TODO*///		}
/*TODO*///	} };
	
	public static ReadHandlerPtr cps1_eeprom_port_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return EEPROM_read_bit();
	} };
	
	public static WriteHandlerPtr cps1_eeprom_port_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/*
		bit 0 = data
		bit 6 = clock
		bit 7 = cs
		*/
		EEPROM_write_bit(data & 0x01);
		EEPROM_set_cs_line((data & 0x80)!=0 ? CLEAR_LINE : ASSERT_LINE);
		EEPROM_set_clock_line((data & 0x40)!=0 ? ASSERT_LINE : CLEAR_LINE);
	} };
	
	
	
	static MemoryReadAddress cps1_readmem[] =
	{
		new MemoryReadAddress( 0x000000, 0x1fffff, MRA_ROM ), /* 68000 ROM */
		new MemoryReadAddress( 0x800000, 0x800001, input_port_4_r ), /* Player input ports */
		new MemoryReadAddress( 0x800010, 0x800011, input_port_4_r ), /* ?? */
		new MemoryReadAddress( 0x800018, 0x80001f, cps1_input_r ), /* Input ports */
		new MemoryReadAddress( 0x800020, 0x800021, MRA_NOP ), /* ? Used by Rockman ? */
		new MemoryReadAddress( 0x800052, 0x800055, forgottn_dial_0_r ), /* forgotten worlds */
		new MemoryReadAddress( 0x80005a, 0x80005d, forgottn_dial_1_r ), /* forgotten worlds */
		new MemoryReadAddress( 0x800176, 0x800177, cps1_input2_r ), /* Extra input ports */
		new MemoryReadAddress( 0x8001fc, 0x8001fd, cps1_input2_r ), /* Input ports (SF Rev E) */
		new MemoryReadAddress( 0x800100, 0x8001ff, cps1_output_r ),   /* Output ports */
		new MemoryReadAddress( 0x900000, 0x92ffff, MRA_BANK3 ),	/* SF2CE executes code from here */
		new MemoryReadAddress( 0xf00000, 0xf0ffff, qsound_rom_r ),		/* Slammasters protection */
		new MemoryReadAddress( 0xf18000, 0xf19fff, qsound_sharedram1_r ),	/* Q RAM */
		new MemoryReadAddress( 0xf1c000, 0xf1c001, cps1_input2_r ),   /* Player 3 controls (later games) */
		new MemoryReadAddress( 0xf1c002, 0xf1c003, cps1_input3_r ),   /* Player 4 controls (later games - muscle bombers) */
		new MemoryReadAddress( 0xf1c006, 0xf1c007, cps1_eeprom_port_r ),
		new MemoryReadAddress( 0xf1e000, 0xf1ffff, qsound_sharedram2_r ),	/* Q RAM */
		new MemoryReadAddress( 0xff0000, 0xffffff, MRA_BANK2 ),   /* RAM */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress cps1_writemem[] =
	{
		new MemoryWriteAddress( 0x000000, 0x1fffff, MWA_ROM ),      /* ROM */
		new MemoryWriteAddress( 0x800030, 0x800031, cps1_coinctrl_w ),
		new MemoryWriteAddress( 0x800040, 0x800041, forgottn_dial_0_reset_w ),
		new MemoryWriteAddress( 0x800048, 0x800049, forgottn_dial_1_reset_w ),
		new MemoryWriteAddress( 0x800180, 0x800181, soundlatch_w ),  /* Sound command */
		new MemoryWriteAddress( 0x800188, 0x800189, cps1_sound_fade_w ),
		new MemoryWriteAddress( 0x800100, 0x8001ff, cps1_output_w, cps1_output, cps1_output_size ),  /* Output ports */
		new MemoryWriteAddress( 0x900000, 0x92ffff, MWA_BANK3, cps1_gfxram, cps1_gfxram_size ),
		new MemoryWriteAddress( 0xf18000, 0xf19fff, qsound_sharedram1_w ), /* Q RAM */
		new MemoryWriteAddress( 0xf1c004, 0xf1c005, cpsq_coinctrl2_w ),   /* Coin control2 (later games) */
		new MemoryWriteAddress( 0xf1c006, 0xf1c007, cps1_eeprom_port_w ),
		new MemoryWriteAddress( 0xf1e000, 0xf1ffff, qsound_sharedram2_w ), /* Q RAM */
		new MemoryWriteAddress( 0xff0000, 0xffffff, MWA_BANK2 ),        /* RAM */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),
		new MemoryReadAddress( 0xd000, 0xd7ff, MRA_RAM ),
		new MemoryReadAddress( 0xf001, 0xf001, YM2151_status_port_0_r ),
		new MemoryReadAddress( 0xf002, 0xf002, OKIM6295_status_0_r ),
		new MemoryReadAddress( 0xf008, 0xf008, soundlatch_r ),
		new MemoryReadAddress( 0xf00a, 0xf00a, cps1_snd_fade_timer_r ), /* Sound timer fade */
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xd000, 0xd7ff, MWA_RAM ),
		new MemoryWriteAddress( 0xf000, 0xf000, YM2151_register_port_0_w ),
		new MemoryWriteAddress( 0xf001, 0xf001, YM2151_data_port_0_w ),
		new MemoryWriteAddress( 0xf002, 0xf002, OKIM6295_data_0_w ),
		new MemoryWriteAddress( 0xf004, 0xf004, cps1_snd_bankswitch_w ),
	//	new MemoryWriteAddress( 0xf006, 0xf006, MWA_NOP ), /* ???? Unknown ???? */
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	static MemoryReadAddress qsound_readmem[] =
	{
		new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new MemoryReadAddress( 0x8000, 0xbfff, MRA_BANK1 ),  /* banked (contains music data) */
		new MemoryReadAddress( 0xc000, 0xcfff, MRA_RAM ),
		new MemoryReadAddress( 0xd007, 0xd007, qsound_status_r ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_RAM ),
		new MemoryReadAddress( -1 )  /* end of table */
	};
	
	static MemoryWriteAddress qsound_writemem[] =
	{
		new MemoryWriteAddress( 0x0000, 0xbfff, MWA_ROM ),
		new MemoryWriteAddress( 0xc000, 0xcfff, MWA_RAM, qsound_sharedram1 ),
		new MemoryWriteAddress( 0xd000, 0xd000, qsound_data_h_w ),
		new MemoryWriteAddress( 0xd001, 0xd001, qsound_data_l_w ),
		new MemoryWriteAddress( 0xd002, 0xd002, qsound_cmd_w ),
		new MemoryWriteAddress( 0xd003, 0xd003, qsound_banksw_w ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_RAM, qsound_sharedram2 ),
		new MemoryWriteAddress( -1 )  /* end of table */
	};
	
	
	
	static InputPortPtr input_ports_forgottn = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "2C_2C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "2C_2C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x40, IP_ACTIVE_LOW );
		PORT_DIPNAME( 0x80, 0x80, "Freeze" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START(); 
		PORT_ANALOGX( 0x0fff, 0x0000, IPT_DIAL | IPF_PLAYER1, 100, 20, 0, 0, KEYCODE_Z, KEYCODE_X, 0, 0 );
	
		PORT_START(); 
		PORT_ANALOGX( 0x0fff, 0x0000, IPT_DIAL | IPF_PLAYER2, 100, 20, 0, 0, KEYCODE_N, KEYCODE_M, 0, 0 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ghouls = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 ); /* Service, but it doesn't give any credit */
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Upright") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Easier" );
		PORT_DIPSETTING(    0x05, "Very Easy" );
		PORT_DIPSETTING(    0x06, "Easy" );
		PORT_DIPSETTING(    0x07, "Normal" );
		PORT_DIPSETTING(    0x03, "Difficult" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x01, "Very Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x20, "10K, 30K and every 30K" );
		PORT_DIPSETTING(    0x10, "20K, 50K and every 70K" );
		PORT_DIPSETTING(    0x30, "30K, 60K and every 70K" );
		PORT_DIPSETTING(    0x00, "40K, 70K and every 80K" );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_strider = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0xc0, "Upright 1 Player" );
		PORT_DIPSETTING(    0x80, "Upright 2 Players" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		/* 0x40 Cocktail */
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Easiest" );
		PORT_DIPSETTING(    0x05, "Easier" );
		PORT_DIPSETTING(    0x06, "Easy" );
		PORT_DIPSETTING(    0x07, "Normal" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x01, "Harder" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		/* TODO: this doesn't seem to work */
		PORT_DIPNAME( 0x08, 0x00, "Continue Coinage ?" );
		PORT_DIPSETTING(    0x00, "1 Coin" );
		PORT_DIPSETTING(    0x08, "2 Coins" );
		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x10, "20000 60000" );
		PORT_DIPSETTING(    0x00, "30000 60000" );
		PORT_DIPSETTING(    0x30, "20000 40000 60000" );
		PORT_DIPSETTING(    0x20, "30000 50000 70000" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x03, "3" );
		PORT_DIPSETTING(    0x02, "4" );
		PORT_DIPSETTING(    0x01, "5" );
		PORT_DIPSETTING(    0x00, "6" );
		PORT_DIPNAME( 0x04, 0x04, "Freeze" );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_dwj = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		/* 0x00 2 Coins/1 credit for both coin ports */
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Very Easy" );
		PORT_DIPSETTING(    0x05, "Easy 2" );
		PORT_DIPSETTING(    0x06, "Easy 1" );
		PORT_DIPSETTING(    0x07, "Normal" );
		PORT_DIPSETTING(    0x03, "Difficult 1" );
		PORT_DIPSETTING(    0x02, "Difficult 2" );
		PORT_DIPSETTING(    0x01, "Difficult 3" );
		PORT_DIPSETTING(    0x00, "Very difficult" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x01, 0x01, "Freeze" );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, "Turbo Mode" );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_willow = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "2 Coins/1 Credit (1 to continue)" );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "2 Coins/1 Credit (1 to continue)" );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Cabinet") );
		PORT_DIPSETTING(    0xc0, "Upright 1 Player" );
		PORT_DIPSETTING(    0x80, "Upright 2 Players" );
		PORT_DIPSETTING(    0x00, DEF_STR( "Cocktail") );
		/* 0x40 Cocktail */
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x04, "Very easy" );
		PORT_DIPSETTING(    0x05, "Easier" );
		PORT_DIPSETTING(    0x06, "Easy" );
		PORT_DIPSETTING(    0x07, "Normal" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x01, "Harder" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x18, 0x18, "Nando Speed" );
		PORT_DIPSETTING(    0x10, "Slow" );
		PORT_DIPSETTING(    0x18, "Normal" );
		PORT_DIPSETTING(    0x08, "Fast" );
		PORT_DIPSETTING(    0x00, "Very Fast" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") ); /* Unused ? */
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x20, DEF_STR( "Unknown") ); /* Unused ? */
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, "Stage Magic Continue (power up?)" );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		/* The test mode reports Stage Magic, a file with dip says if
		 power up are on the player gets sword and magic item without having
		 to buy them. To test */
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x02, "1" );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x01, "3" );
		PORT_DIPSETTING(    0x00, "4" );
		PORT_DIPNAME( 0x0c, 0x0c, "Vitality" );
		PORT_DIPSETTING(    0x00, "2" );
		PORT_DIPSETTING(    0x0c, "3" );
		PORT_DIPSETTING(    0x08, "4" );
		PORT_DIPSETTING(    0x04, "5" );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_unsquad = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x01, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "2 Coins/1 Credit (1 to continue)" );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x08, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x00, "2 Coins/1 Credit (1 to continue)" );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x07, "Super Easy" );
		PORT_DIPSETTING(    0x06, "Very Easy" );
		PORT_DIPSETTING(    0x05, "Easy" );
		PORT_DIPSETTING(    0x04, "Normal" );
		PORT_DIPSETTING(    0x03, "Difficult" );
		PORT_DIPSETTING(    0x02, "Very Difficult" );
		PORT_DIPSETTING(    0x01, "Super Difficult" );
		PORT_DIPSETTING(    0x00, "Ultra Super Difficult" );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_ffight = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x04, "Difficulty Level 1" );
		PORT_DIPSETTING(    0x07, "Very easy" );
		PORT_DIPSETTING(    0x06, "Easier" );
		PORT_DIPSETTING(    0x05, "Easy" );
		PORT_DIPSETTING(    0x04, "Normal" );
		PORT_DIPSETTING(    0x03, "Medium" );
		PORT_DIPSETTING(    0x02, "Hard" );
		PORT_DIPSETTING(    0x01, "Harder" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x18, 0x10, "Difficulty Level 2" );
		PORT_DIPSETTING(    0x18, "Easy" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x08, "Hard" );
		PORT_DIPSETTING(    0x00, "Hardest" );
		PORT_DIPNAME( 0x60, 0x60, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(    0x60, "100k" );
		PORT_DIPSETTING(    0x40, "200k" );
		PORT_DIPSETTING(    0x20, "100k and every 200k" );
		PORT_DIPSETTING(    0x00, "None" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x03, "2" );
		PORT_DIPSETTING(    0x02, "3" );
		PORT_DIPSETTING(    0x01, "4" );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_1941 = new InputPortPtr(){ public void handler() { 
		PORT_START();       /* IN0 */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();       /* DSWA */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
	
		PORT_START();       /* DSWB */
		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(    0x07, "0 (Easier)" );
		PORT_DIPSETTING(    0x06, "1" );
		PORT_DIPSETTING(    0x05, "2" );
		PORT_DIPSETTING(    0x04, "3" );
		PORT_DIPSETTING(    0x03, "4" );
		PORT_DIPSETTING(    0x02, "5" );
		PORT_DIPSETTING(    0x01, "6" );
		PORT_DIPSETTING(    0x00, "7 (Harder)" );
		PORT_DIPNAME( 0x18, 0x18, "Life Bar" );
		PORT_DIPSETTING(    0x18, "More Slowly" );
		PORT_DIPSETTING(    0x10, "Slowly" );
		PORT_DIPSETTING(    0x08, "Quickly" );
		PORT_DIPSETTING(    0x00, "More Quickly" );
		PORT_DIPNAME( 0x60, 0x60, "Bullet's Speed" );
		PORT_DIPSETTING(    0x60, "Very Slow" );
		PORT_DIPSETTING(    0x40, "Slow" );
		PORT_DIPSETTING(    0x20, "Fast" );
		PORT_DIPSETTING(    0x00, "Very Fast" );
		PORT_DIPNAME( 0x80, 0x80, "Initial Vitality" );
		PORT_DIPSETTING(    0x80, "3 Bars" );
		PORT_DIPSETTING(    0x00, "4 Bars" );
	
		PORT_START();       /* DSWC */
		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
	
		PORT_START(); 
		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
	INPUT_PORTS_END(); }}; 
	
/*TODO*///	static InputPortPtr input_ports_mercs = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x00, "0" );
/*TODO*///		PORT_DIPSETTING(    0x08, "1" );
/*TODO*///		PORT_DIPSETTING(    0x10, "2" );
/*TODO*///		PORT_DIPSETTING(    0x38, "3" );
/*TODO*///		PORT_DIPSETTING(    0x30, "4" );
/*TODO*///		PORT_DIPSETTING(    0x28, "5" );
/*TODO*///		PORT_DIPSETTING(    0x20, "6" );
/*TODO*///		PORT_DIPSETTING(    0x18, "7" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Coin Slots" );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "Max Players" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPSETTING(    0x10, "3" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x00, "0" );
/*TODO*///		PORT_DIPSETTING(    0x01, "1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2" );
/*TODO*///		PORT_DIPSETTING(    0x03, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_DIPSETTING(    0x05, "5" );
/*TODO*///		PORT_DIPSETTING(    0x06, "6" );
/*TODO*///		PORT_DIPSETTING(    0x07, "7" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* Player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_mtwins = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Super Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Super Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Ultra Super Difficult" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x20, "1" );
/*TODO*///		PORT_DIPSETTING(    0x10, "2" );
/*TODO*///		PORT_DIPSETTING(    0x00, "3" );
/*TODO*///		/*  0x30 gives 1 life */
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_msword = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, "Level 1" );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, "Level 2" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x28, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x38, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Stage Select" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, "Vitality" );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x02, "3" );
/*TODO*///		PORT_DIPSETTING(    0x01, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_cawing = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, "Difficulty Level (Enemy Strength); )
/*TODO*///		PORT_DIPSETTING(    0x07, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Easy 2" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy 1" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult 1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Difficult 2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Difficult 3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Very Difficult" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x10, "Difficulty Level (Player Strength); )
/*TODO*///		PORT_DIPSETTING(    0x10, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Very Difficult" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_nemo = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Easy 1" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy 2" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult 1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Difficult 2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Difficult 3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Very Difficult" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Life Bar" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Minimun" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Medium" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Maximum" );
/*TODO*///		/* 0x10 gives Medium */
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x02, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_sf2 = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN  );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN  );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easier" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x02, "3" );
/*TODO*///		PORT_DIPSETTING(    0x01, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* Extra buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_sf2j = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN  );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_SERVICE );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN  );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easier" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	    PORT_DIPNAME( 0x08, 0x00, "2 Players Game" );
/*TODO*///	    PORT_DIPSETTING(    0x08, "1 Credit/No Continue" );
/*TODO*///	    PORT_DIPSETTING(    0x00, "2 Credits/Winner Continue" );
/*TODO*///	    PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///	    PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x02, "3" );
/*TODO*///		PORT_DIPSETTING(    0x01, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* Extra buttons */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_3wonders = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, "Action Lives" );
/*TODO*///		PORT_DIPSETTING(    0x03, "1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "5" );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, "Action Difficulty" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, "Shooting Lives" );
/*TODO*///		PORT_DIPSETTING(    0x30, "1" );
/*TODO*///		PORT_DIPSETTING(    0x20, "2" );
/*TODO*///		PORT_DIPSETTING(    0x10, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "5" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Shooting Difficulty" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x80, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x40, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, "Puzzle Lives" );
/*TODO*///		PORT_DIPSETTING(    0x03, "1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "5" );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, "Puzzle Difficulty" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_kod = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );/* Service Coin, not player 3 */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* Test */
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Coin Slots" );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, "Max Players" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPSETTING(    0x10, "3" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Medium" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "8" );
/*TODO*///		PORT_DIPSETTING(    0x08, "7" );
/*TODO*///		PORT_DIPSETTING(    0x10, "6" );
/*TODO*///		PORT_DIPSETTING(    0x18, "5" );
/*TODO*///		PORT_DIPSETTING(    0x20, "4" );
/*TODO*///		PORT_DIPSETTING(    0x28, "3" );
/*TODO*///		PORT_DIPSETTING(    0x38, "2" );
/*TODO*///		PORT_DIPSETTING(    0x30, "1" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x80, "80k and every 400k" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "100k and every 450k" );
/*TODO*///		PORT_DIPSETTING(    0x40, "160k and every 450k" );
/*TODO*///		PORT_DIPSETTING(    0x00, "None" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* Player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_captcomm = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, "Difficulty 1" );
/*TODO*///		PORT_DIPSETTING(    0x07, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Easy 1" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy 2" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Difficulty 2" );
/*TODO*///		PORT_DIPSETTING(    0x18, "1" );
/*TODO*///		PORT_DIPSETTING(    0x10, "2" );
/*TODO*///		PORT_DIPSETTING(    0x08, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "4" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Max Players" );
/*TODO*///		PORT_DIPSETTING(    0x40, "1" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "2" );
/*TODO*///		PORT_DIPSETTING(    0x80, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "4" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x02, "3" );
/*TODO*///		PORT_DIPSETTING(    0x01, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_knights = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN4 );/* service */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );/* TEST */
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unused") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, "Player speed and vitality consumption" );
/*TODO*///		PORT_DIPSETTING(    0x07, "Very easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Easier" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Medium" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Harder" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, "Enemy's vitality and attack power" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x08, "Easier" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x38, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x30, "Medium" );
/*TODO*///		PORT_DIPSETTING(    0x28, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x20, "Harder" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Coin Slots" );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x40, "3" );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, "Max Players" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPSETTING(    0x80, "3" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x03, "2" );
/*TODO*///		PORT_DIPSETTING(    0x02, "3" );
/*TODO*///		PORT_DIPSETTING(    0x01, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* Player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_varth = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coin_A") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Coin_B") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x30, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x28, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Easy 1" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy 2" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hardest" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, DEF_STR( "Bonus_Life") );
/*TODO*///		PORT_DIPSETTING(    0x18, "600k and every 1.400k" );
/*TODO*///		PORT_DIPSETTING(    0x10, "600k 2.000k and 4500k" );
/*TODO*///		PORT_DIPSETTING(    0x08, "1.200k 3.500k" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2000k only" );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x02, "1" );
/*TODO*///		PORT_DIPSETTING(    0x01, "2" );
/*TODO*///		PORT_DIPSETTING(    0x03, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_cworld2j = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x38, 0x38, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x38, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, "Test Mode (Use with Service Mode); )
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x06, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x06, "0" );
/*TODO*///		PORT_DIPSETTING(    0x05, "1" );
/*TODO*///		PORT_DIPSETTING(    0x04, "2" );
/*TODO*///		PORT_DIPSETTING(    0x03, "3" );
/*TODO*///		PORT_DIPSETTING(    0x02, "4" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Extend" );
/*TODO*///		PORT_DIPSETTING(    0x18, "N" );
/*TODO*///		PORT_DIPSETTING(    0x10, "E" );
/*TODO*///		PORT_DIPSETTING(    0x00, "D" );
/*TODO*///		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x80, "2" );
/*TODO*///		PORT_DIPSETTING(    0xe0, "3" );
/*TODO*///		PORT_DIPSETTING(    0xa0, "4" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "5" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_wof = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BITX(0x40, 0x40, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0xf7, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///	    PORT_START();       /* Player 3 */
/*TODO*///	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_dino = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BITX(0x40, 0x40, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0xf7, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///	    PORT_START();       /* Player 3 */
/*TODO*///	    PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///	    PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_punisher = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BITX(0x40, 0x40, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0xf7, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_slammast = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BITX(0x40, 0x40, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_BIT( 0xf7, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER4 );
/*TODO*///	
/*TODO*///		PORT_START();      /* Player 3 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///	
/*TODO*///		PORT_START();      /* Player 4 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER4 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_pnickj = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Coin Slots" );
/*TODO*///		PORT_DIPSETTING(    0x08, "1" );
/*TODO*///		PORT_DIPSETTING(    0x00, "2" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x04, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "Easiest" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Hard" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Hardest" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Master Level" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x00, "Unknkown" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x30, 0x30, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x00, "1" );
/*TODO*///		PORT_DIPSETTING(    0x10, "2" );
/*TODO*///		PORT_DIPSETTING(    0x20, "3" );
/*TODO*///		PORT_DIPSETTING(    0x30, "4" );
/*TODO*///		PORT_DIPNAME( 0xc0, 0xc0, "Vs Play Mode" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "1 Game Match" );
/*TODO*///		PORT_DIPSETTING(    0x80, "3 Games Match" );
/*TODO*///		PORT_DIPSETTING(    0x40, "5 Games Match" );
/*TODO*///		PORT_DIPSETTING(    0x00, "7 Games Match" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x03, "1" );
/*TODO*///		PORT_DIPSETTING(    0x02, "2" );
/*TODO*///		PORT_DIPSETTING(    0x01, "3" );
/*TODO*///		PORT_DIPSETTING(    0x00, "4" );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_qad = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x06, DEF_STR( "Difficulty") );
/*TODO*///	//	PORT_DIPSETTING(    0x07, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x06, "Very Easy" );
/*TODO*///		PORT_DIPSETTING(    0x05, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x04, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x03, "Hard" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Very Hard" );
/*TODO*///	//	PORT_DIPSETTING(    0x01, "Very Hard" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "Very Hard" );
/*TODO*///		PORT_DIPNAME( 0x18, 0x18, "Wisdom" );
/*TODO*///		PORT_DIPSETTING(    0x18, "Low" );
/*TODO*///		PORT_DIPSETTING(    0x10, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x08, "High" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Brilliant" );
/*TODO*///		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x60, "1" );
/*TODO*///		PORT_DIPSETTING(    0x80, "2" );
/*TODO*///		PORT_DIPSETTING(    0xa0, "3" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "4" );
/*TODO*///		PORT_DIPSETTING(    0xe0, "5" );
/*TODO*///	//	PORT_DIPSETTING(    0x40, "1" );
/*TODO*///	//	PORT_DIPSETTING(    0x20, "1" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "1" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_qadj = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "0" );
/*TODO*///		PORT_DIPSETTING(    0x06, "1" );
/*TODO*///		PORT_DIPSETTING(    0x05, "2" );
/*TODO*///		PORT_DIPSETTING(    0x04, "3" );
/*TODO*///		PORT_DIPSETTING(    0x03, "4" );
/*TODO*///	//	PORT_DIPSETTING(    0x02, "4" );
/*TODO*///	//	PORT_DIPSETTING(    0x01, "4" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "4" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0xa0, "1" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "2" );
/*TODO*///		PORT_DIPSETTING(    0xe0, "3" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "1" );
/*TODO*///	//	PORT_DIPSETTING(    0x20, "1" );
/*TODO*///	//	PORT_DIPSETTING(    0x80, "1" );
/*TODO*///	//	PORT_DIPSETTING(    0x40, "2" );
/*TODO*///	//	PORT_DIPSETTING(    0x60, "3" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_qtono2 = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_COIN3 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_COIN4 );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_START3 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_START4 );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x07, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x06, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x05, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x03, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "2 Coins to Start, 1 to Continue" );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x07, 0x07, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x07, "1" );
/*TODO*///		PORT_DIPSETTING(    0x06, "2" );
/*TODO*///		PORT_DIPSETTING(    0x05, "3" );
/*TODO*///		PORT_DIPSETTING(    0x04, "4" );
/*TODO*///		PORT_DIPSETTING(    0x03, "5" );
/*TODO*///		PORT_DIPSETTING(    0x02, "6" );
/*TODO*///		PORT_DIPSETTING(    0x01, "7" );
/*TODO*///		PORT_DIPSETTING(    0x00, "8" );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0xe0, 0xe0, DEF_STR( "Lives") );
/*TODO*///		PORT_DIPSETTING(    0x60, "1" );
/*TODO*///		PORT_DIPSETTING(    0x80, "2" );
/*TODO*///		PORT_DIPSETTING(    0xe0, "3" );
/*TODO*///		PORT_DIPSETTING(    0xa0, "4" );
/*TODO*///		PORT_DIPSETTING(    0xc0, "5" );
/*TODO*///	//	PORT_DIPSETTING(    0x40, "?" );
/*TODO*///	//	PORT_DIPSETTING(    0x20, "?" );
/*TODO*///	//	PORT_DIPSETTING(    0x00, "?" );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Free_Play") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Yes") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_pang3 = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BITX(0x40, 0x40, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE )
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB (not used, EEPROM) */
/*TODO*///		PORT_BIT( 0xff, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, "Freeze" );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_megaman = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );
/*TODO*///		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_SERVICE1 );
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0x1f, 0x1f, DEF_STR( "Coinage") );
/*TODO*///		PORT_DIPSETTING(    0x0f, DEF_STR( "9C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "8C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x11, DEF_STR( "7C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x12, DEF_STR( "6C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x13, DEF_STR( "5C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x14, DEF_STR( "4C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x15, DEF_STR( "3C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x16, DEF_STR( "2C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x0e, "2 Coins/1 Credit - 1 to continue (if on); )
/*TODO*///		PORT_DIPSETTING(    0x1f, DEF_STR( "1C_1C") );
/*TODO*///		PORT_DIPSETTING(    0x1e, DEF_STR( "1C_2C") );
/*TODO*///		PORT_DIPSETTING(    0x1d, DEF_STR( "1C_3C") );
/*TODO*///		PORT_DIPSETTING(    0x1c, DEF_STR( "1C_4C") );
/*TODO*///		PORT_DIPSETTING(    0x1b, DEF_STR( "1C_5C") );
/*TODO*///		PORT_DIPSETTING(    0x1a, DEF_STR( "1C_6C") );
/*TODO*///		PORT_DIPSETTING(    0x19, DEF_STR( "1C_7C") );
/*TODO*///		PORT_DIPSETTING(    0x18, DEF_STR( "1C_8C") );
/*TODO*///		PORT_DIPSETTING(    0x17, DEF_STR( "1C_9C") );
/*TODO*///		PORT_DIPSETTING(    0x0d, DEF_STR( "Free_Play") );
/*TODO*///		/* 0x00 to 0x0c 1 Coin/1 Credit */
/*TODO*///		PORT_DIPNAME( 0x60, 0x20, "2 Player Game" );
/*TODO*///		PORT_DIPSETTING(    0x20, "1 Credit" );
/*TODO*///		PORT_DIPSETTING(    0x40, "2 Credits" );
/*TODO*///		PORT_DIPSETTING(    0x60, "2 Credits - pl1 may play on right" );
/*TODO*///		   /* Unused 0x00 */
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0x03, 0x03, DEF_STR( "Difficulty") );
/*TODO*///		PORT_DIPSETTING(    0x03, "Easy" );
/*TODO*///		PORT_DIPSETTING(    0x02, "Normal" );
/*TODO*///		PORT_DIPSETTING(    0x01, "Difficult" );
/*TODO*///		PORT_DIPSETTING(    0x00, "Hard" );
/*TODO*///		PORT_DIPNAME( 0x0c, 0x0c, "Time" );
/*TODO*///		PORT_DIPSETTING(    0x0c, "100" );
/*TODO*///		PORT_DIPSETTING(    0x08, "90" );
/*TODO*///		PORT_DIPSETTING(    0x04, "70" );
/*TODO*///		PORT_DIPSETTING(    0x00, "60" );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, "Voice" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x80, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0x01, 0x01, DEF_STR( "Flip_Screen") );
/*TODO*///		PORT_DIPSETTING(    0x01, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x02, 0x02, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x02, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x04, 0x04, "Allow Continue" );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "No") );
/*TODO*///		PORT_DIPSETTING(    0x04, DEF_STR( "Yes") );
/*TODO*///		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x08, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x10, 0x10, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x10, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Demo_Sounds") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x20, DEF_STR( "On") );
/*TODO*///		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///		PORT_SERVICE( 0x80, IP_ACTIVE_LOW );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_UNKNOWN );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	static InputPortPtr input_ports_sfzch = new InputPortPtr(){ public void handler() { 
/*TODO*///		PORT_START();       /* IN0 */
/*TODO*///		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON5 | IPF_PLAYER2 );
/*TODO*///		PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, "Pause", KEYCODE_F1, IP_JOY_NONE );/* pause */
/*TODO*///		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_SERVICE  );/* pause */
/*TODO*///		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );
/*TODO*///		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );
/*TODO*///		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_BUTTON6 | IPF_PLAYER2  );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWA */
/*TODO*///		PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWB */
/*TODO*///		PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START();       /* DSWC */
/*TODO*///		PORT_DIPNAME( 0xff, 0xff, DEF_STR( "Unknown") );
/*TODO*///		PORT_DIPSETTING(    0xff, DEF_STR( "Off") );
/*TODO*///		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
/*TODO*///	
/*TODO*///		PORT_START(); 
/*TODO*///		PORT_BIT( 0x0001, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0002, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0004, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0008, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0010, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0020, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0040, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0080, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER1 );
/*TODO*///		PORT_BIT( 0x0100, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0200, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0400, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x0800, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x1000, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x2000, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x4000, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER2 );
/*TODO*///		PORT_BIT( 0x8000, IP_ACTIVE_LOW, IPT_BUTTON4 | IPF_PLAYER2 );
/*TODO*///	INPUT_PORTS_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	/********************************************************************
/*TODO*///	
/*TODO*///				Graphics Layout
/*TODO*///	
/*TODO*///	  These are not really needed, and are used for documentation only.
/*TODO*///	
/*TODO*///	********************************************************************/
/*TODO*///	
/*TODO*///	#define DECODE_GFX 0
	
	static GfxLayout tilelayout8 = new GfxLayout
	(
		8,8,
/*TODO*///	#if DECODE_GFX
/*TODO*///		RGN_FRAC(1,2),
/*TODO*///	#else
		0,
/*TODO*///	#endif
		4,
		new int[] { RGN_FRAC(1,2)+8, RGN_FRAC(1,2)+0, 8, 0 },
		new int[] { 0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
		16*8
	);
	
	static GfxLayout tilelayout16 = new GfxLayout
	(
		16,16,
/*TODO*///	#if DECODE_GFX
/*TODO*///		RGN_FRAC(1,4),
/*TODO*///	#else
		0,
/*TODO*///	#endif
		4,
		new int[] { RGN_FRAC(1,2)+8, RGN_FRAC(1,2)+0, 8, 0 },
		new int[] { RGN_FRAC(1,4)+0, RGN_FRAC(1,4)+1, RGN_FRAC(1,4)+2, RGN_FRAC(1,4)+3,
		  RGN_FRAC(1,4)+4, RGN_FRAC(1,4)+5, RGN_FRAC(1,4)+6, RGN_FRAC(1,4)+7,
		  0, 1, 2, 3, 4, 5, 6, 7 },
		new int[] { 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
				8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
		16*16
	);
	
	static GfxLayout tilelayout32 = new GfxLayout
	(
		32,32,
/*TODO*///	#if DECODE_GFX
/*TODO*///		RGN_FRAC(1,4),
/*TODO*///	#else
		0,
/*TODO*///	#endif
		4,
		new int[] { RGN_FRAC(1,2)+8, RGN_FRAC(1,2)+0, 8, 0 },
		new int[] {
			RGN_FRAC(1,4)+0, RGN_FRAC(1,4)+1, RGN_FRAC(1,4)+2, RGN_FRAC(1,4)+3,
			RGN_FRAC(1,4)+4, RGN_FRAC(1,4)+5, RGN_FRAC(1,4)+6, RGN_FRAC(1,4)+7,
			0, 1, 2, 3, 4, 5, 6, 7,
			16+RGN_FRAC(1,4)+0, 16+RGN_FRAC(1,4)+1, 16+RGN_FRAC(1,4)+2, 16+RGN_FRAC(1,4)+3,
			16+RGN_FRAC(1,4)+4, 16+RGN_FRAC(1,4)+5, 16+RGN_FRAC(1,4)+6, 16+RGN_FRAC(1,4)+7,
			16+0, 16+1, 16+2, 16+3, 16+4, 16+5, 16+6, 16+7
		},
		new int[] {
			 0*32,  1*32,  2*32,  3*32,  4*32,  5*32,  6*32,  7*32,
			 8*32,  9*32, 10*32, 11*32, 12*32, 13*32, 14*32, 15*32,
			16*32, 17*32, 18*32, 19*32, 20*32, 21*32, 22*32, 23*32,
			24*32, 25*32, 26*32, 27*32, 28*32, 29*32, 30*32, 31*32
		},
		32*32
	);
	
	static GfxDecodeInfo cps1_gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout16, 0x000, 32 ),	/* sprites */
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout8,  0x200, 32 ),	/* tiles 8x8 */
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout16, 0x400, 32 ),	/* tiles 16x16 */
		new GfxDecodeInfo( REGION_GFX1, 0, tilelayout32, 0x600, 32 ),	/* tiles 32x32 */
		/* stars use colors 0x800-087ff and 0xa00-0a7ff */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	
	
	static WriteYmHandlerPtr cps1_irq_handler_mus = new WriteYmHandlerPtr() {
            @Override
            public void handler(int irq) {
                cpu_set_irq_line(1,0,irq!=0 ? ASSERT_LINE : CLEAR_LINE);
            }
        };
        
	static YM2151interface ym2151_interface = new YM2151interface
	(
		1,  /* 1 chip */
		3579580,    /* 3.579580 MHz ? */
		new int[] { YM3012_VOL(35,MIXER_PAN_LEFT,35,MIXER_PAN_RIGHT) },
		new WriteYmHandlerPtr[] { cps1_irq_handler_mus }
	);
	
	static OKIM6295interface okim6295_interface_6061 = new OKIM6295interface
	(
		1,  /* 1 chip */
		new int[] { 6061 },
		new int[] { REGION_SOUND1 },
		new int[] { 30 }
	);
	
	static OKIM6295interface okim6295_interface_7576 = new OKIM6295interface
	(
		1,  /* 1 chip */
		new int[] { 7576 },
		new int[] { REGION_SOUND1 },
		new int[] { 30 }
	);
	
	
	
/*TODO*///	/********************************************************************
/*TODO*///	*
/*TODO*///	*  Machine Driver macro
/*TODO*///	*  ====================
/*TODO*///	*
/*TODO*///	*  Abusing the pre-processor.
/*TODO*///	*
/*TODO*///	********************************************************************/
/*TODO*///	
/*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
/*TODO*///	static MachineDriver machine_driver_##DRVNAME = new MachineDriver
/*TODO*///	(																			
/*TODO*///		/* basic machine hardware */											
/*TODO*///		new MachineCPU[] {																		
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU,															
/*TODO*///				CPU_FRQ,														
/*TODO*///				cps1_readmem,cps1_writemem,null,null,									
/*TODO*///				cps1_interrupt, 1												
/*TODO*///			),																	
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,										
/*TODO*///				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
/*TODO*///				sound_readmem,sound_writemem,null,null,								
/*TODO*///				ignore_interrupt,0												
/*TODO*///			)																	
/*TODO*///		},																		
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,										
/*TODO*///		1,																		
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		/* video hardware */													
/*TODO*///		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
/*TODO*///																				
/*TODO*///		cps1_gfxdecodeinfo,														
/*TODO*///		4096, 4096,																
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
/*TODO*///		cps1_eof_callback,														
/*TODO*///		cps1_vh_start,															
/*TODO*///		cps1_vh_stop,															
/*TODO*///		cps1_vh_screenrefresh,													
/*TODO*///																				
/*TODO*///		/* sound hardware */													
/*TODO*///		0,0,0,0,																
/*TODO*///		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
/*TODO*///		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_##OKI_FREQ )					
/*TODO*///		},																		
/*TODO*///		NVRAM																	
/*TODO*///	);
	
	static MachineDriver machine_driver_qsound = new MachineDriver
	(
		new MachineCPU[] {
			new MachineCPU(
				CPU_M68000,
				10000000,	/* ??? */
				cps1_readmem,cps1_writemem,null,null,
				cps1_qsound_interrupt, 1  /* ??? interrupts per frame */
			),
			new MachineCPU(
				CPU_Z80,	/* can't use CPU_AUDIO_CPU, slammast requires the Z80 for protection */
				6000000,  /* 6 MHz ??? TODO: find real FRQ */
				qsound_readmem,qsound_writemem,null,null,
				interrupt,4
			)
		},
		60, DEFAULT_60HZ_VBLANK_DURATION,
		1,
		null,
	
		/* video hardware */
		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),
	
		cps1_gfxdecodeinfo,
		4096, 4096,
		null,
	
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,
		cps1_eof_callback,
		cps1_vh_start,
		cps1_vh_stop,
		cps1_vh_screenrefresh,
	
		/* sound hardware */
		SOUND_SUPPORTS_STEREO,0,0,0,
		new MachineSound[] {
			new MachineSound(
				SOUND_QSOUND,
				qsound_interface
			)
		},
	
		qsound_nvram_handler
	);
	
	
        //	MACHINE_DRIVER( forgottn, CPU_M68000,   10000000, 6061, 0 )
        /*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
	static MachineDriver machine_driver_forgottn = new MachineDriver
	(																			
		/* basic machine hardware */											
		new MachineCPU[] {																		
			new MachineCPU(																	
				CPU_M68000,															
				10000000,														
				cps1_readmem,cps1_writemem,null,null,									
				cps1_interrupt, 1												
			),																	
			new MachineCPU(																	
				CPU_Z80 | CPU_AUDIO_CPU,										
				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
				sound_readmem,sound_writemem,null,null,								
				ignore_interrupt,0												
			)																	
		},																		
		60, DEFAULT_60HZ_VBLANK_DURATION,										
		1,																		
		null,																		
																				
		/* video hardware */													
		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
																				
		cps1_gfxdecodeinfo,														
		4096, 4096,																
		null,																		
																				
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
		cps1_eof_callback,														
		cps1_vh_start,															
		cps1_vh_stop,															
		cps1_vh_screenrefresh,													
																				
		/* sound hardware */													
		0,0,0,0,																
		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_6061 )					
		},																		
		null																	
	);
	
/*TODO*///	MACHINE_DRIVER( cps1,     CPU_M68000,   10000000, 7576, 0 )	/* 10 MHz should be the "standard" freq */
        /*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
	static MachineDriver machine_driver_cps1 = new MachineDriver
	(																			
		/* basic machine hardware */											
		new MachineCPU[] {																		
			new MachineCPU(																	
				CPU_M68000,															
				10000000,														
				cps1_readmem,cps1_writemem,null,null,									
				cps1_interrupt, 1												
			),																	
			new MachineCPU(																	
				CPU_Z80 | CPU_AUDIO_CPU,										
				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
				sound_readmem,sound_writemem,null,null,								
				ignore_interrupt,0												
			)																	
		},																		
		60, DEFAULT_60HZ_VBLANK_DURATION,										
		1,																		
		null,																		
																				
		/* video hardware */													
		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
																				
		cps1_gfxdecodeinfo,														
		4096, 4096,																
		null,																		
																				
		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
		cps1_eof_callback,														
		cps1_vh_start,															
		cps1_vh_stop,															
		cps1_vh_screenrefresh,													
																				
		/* sound hardware */													
		0,0,0,0,																
		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_7576 )					
		},																		
		null																	
	);
	
        
/*TODO*///	MACHINE_DRIVER( sf2,      CPU_M68000,   12000000, 7576, 0 )	/* 12 MHz */
        /*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
/*TODO*///	static MachineDriver machine_driver_##DRVNAME = new MachineDriver
/*TODO*///	(																			
/*TODO*///		/* basic machine hardware */											
/*TODO*///		new MachineCPU[] {																		
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU,															
/*TODO*///				CPU_FRQ,														
/*TODO*///				cps1_readmem,cps1_writemem,null,null,									
/*TODO*///				cps1_interrupt, 1												
/*TODO*///			),																	
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,										
/*TODO*///				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
/*TODO*///				sound_readmem,sound_writemem,null,null,								
/*TODO*///				ignore_interrupt,0												
/*TODO*///			)																	
/*TODO*///		},																		
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,										
/*TODO*///		1,																		
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		/* video hardware */													
/*TODO*///		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
/*TODO*///																				
/*TODO*///		cps1_gfxdecodeinfo,														
/*TODO*///		4096, 4096,																
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
/*TODO*///		cps1_eof_callback,														
/*TODO*///		cps1_vh_start,															
/*TODO*///		cps1_vh_stop,															
/*TODO*///		cps1_vh_screenrefresh,													
/*TODO*///																				
/*TODO*///		/* sound hardware */													
/*TODO*///		0,0,0,0,																
/*TODO*///		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
/*TODO*///		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_##OKI_FREQ )					
/*TODO*///		},																		
/*TODO*///		NVRAM																	
/*TODO*///	);
/*TODO*///	
/*TODO*///	MACHINE_DRIVER( sf2accp2, CPU_M68EC020, 12000000, 7576, 0 )	/* 12 MHz */
        /*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
/*TODO*///	static MachineDriver machine_driver_##DRVNAME = new MachineDriver
/*TODO*///	(																			
/*TODO*///		/* basic machine hardware */											
/*TODO*///		new MachineCPU[] {																		
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU,															
/*TODO*///				CPU_FRQ,														
/*TODO*///				cps1_readmem,cps1_writemem,null,null,									
/*TODO*///				cps1_interrupt, 1												
/*TODO*///			),																	
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,										
/*TODO*///				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
/*TODO*///				sound_readmem,sound_writemem,null,null,								
/*TODO*///				ignore_interrupt,0												
/*TODO*///			)																	
/*TODO*///		},																		
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,										
/*TODO*///		1,																		
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		/* video hardware */													
/*TODO*///		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
/*TODO*///																				
/*TODO*///		cps1_gfxdecodeinfo,														
/*TODO*///		4096, 4096,																
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
/*TODO*///		cps1_eof_callback,														
/*TODO*///		cps1_vh_start,															
/*TODO*///		cps1_vh_stop,															
/*TODO*///		cps1_vh_screenrefresh,													
/*TODO*///																				
/*TODO*///		/* sound hardware */													
/*TODO*///		0,0,0,0,																
/*TODO*///		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
/*TODO*///		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_##OKI_FREQ )					
/*TODO*///		},																		
/*TODO*///		NVRAM																	
/*TODO*///	);
/*TODO*///	
/*TODO*///	MACHINE_DRIVER( pang3,    CPU_M68000,   10000000, 7576, pang3_nvram_handler )	/* 10 MHz?? */
        /*TODO*///	#define MACHINE_DRIVER(DRVNAME,CPU,CPU_FRQ,OKI_FREQ,NVRAM)					
/*TODO*///	static MachineDriver machine_driver_##DRVNAME = new MachineDriver
/*TODO*///	(																			
/*TODO*///		/* basic machine hardware */											
/*TODO*///		new MachineCPU[] {																		
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU,															
/*TODO*///				CPU_FRQ,														
/*TODO*///				cps1_readmem,cps1_writemem,null,null,									
/*TODO*///				cps1_interrupt, 1												
/*TODO*///			),																	
/*TODO*///			new MachineCPU(																	
/*TODO*///				CPU_Z80 | CPU_AUDIO_CPU,										
/*TODO*///				4000000,  /* 4 MHz ??? TODO: find real FRQ */					
/*TODO*///				sound_readmem,sound_writemem,null,null,								
/*TODO*///				ignore_interrupt,0												
/*TODO*///			)																	
/*TODO*///		},																		
/*TODO*///		60, DEFAULT_60HZ_VBLANK_DURATION,										
/*TODO*///		1,																		
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		/* video hardware */													
/*TODO*///		0x30*8+32*2, 0x1c*8+32*3, new rectangle( 32, 32+0x30*8-1, 32+16, 32+16+0x1c*8-1 ),	
/*TODO*///																				
/*TODO*///		cps1_gfxdecodeinfo,														
/*TODO*///		4096, 4096,																
/*TODO*///		null,																		
/*TODO*///																				
/*TODO*///		VIDEO_TYPE_RASTER | VIDEO_MODIFIES_PALETTE,								
/*TODO*///		cps1_eof_callback,														
/*TODO*///		cps1_vh_start,															
/*TODO*///		cps1_vh_stop,															
/*TODO*///		cps1_vh_screenrefresh,													
/*TODO*///																				
/*TODO*///		/* sound hardware */													
/*TODO*///		0,0,0,0,																
/*TODO*///		new MachineSound[] { new MachineSound( SOUND_YM2151,  ym2151_interface ),									
/*TODO*///		  new MachineSound( SOUND_OKIM6295,  okim6295_interface_##OKI_FREQ )					
/*TODO*///		},																		
/*TODO*///		NVRAM																	
/*TODO*///	);
/*TODO*///	
/*TODO*///	
	
	
	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	
	static int CODE_SIZE = 0x200000;
	
	static RomLoadPtr rom_forgottn = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( CODE_SIZE, REGION_CPU1 );
		ROM_LOAD_EVEN( "lwu11a",        0x00000, 0x20000, 0xddf78831 );
		ROM_LOAD_ODD ( "lwu15a",        0x00000, 0x20000, 0xf7ce2097 );
		ROM_LOAD_EVEN( "lwu10a",        0x40000, 0x20000, 0x8cb38c81 );
		ROM_LOAD_ODD ( "lwu14a",        0x40000, 0x20000, 0xd70ef9fd );
		ROM_LOAD_WIDE_SWAP( "lw-07",         0x80000, 0x80000, 0xfd252a26 );
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "lw-06",         0x000000, 0x80000, 0x5b9edffc );
		ROM_LOAD( "lw-05",         0x080000, 0x80000, 0xe4552fd7 );
		ROM_LOAD( "lw-02",         0x100000, 0x80000, 0x43e6c5c8 );
		ROM_LOAD( "lw-01",         0x180000, 0x80000, 0x0318f298 );
		ROM_LOAD( "lw-13",         0x200000, 0x80000, 0x8e058ef5 );
		ROM_LOAD( "lw-12",         0x280000, 0x80000, 0x8e6a832b );
		ROM_LOAD( "lw-09",         0x300000, 0x80000, 0x899cb4ad );
		ROM_LOAD( "lw-08",         0x380000, 0x80000, 0x25a8e43c );
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "lwu00",         0x00000, 0x08000, 0x59df2a63 );
		ROM_CONTINUE(              0x10000, 0x08000 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "lw-03u",        0x00000, 0x20000, 0x807d051f );
		ROM_LOAD( "lw-04u",        0x20000, 0x20000, 0xe6cd098e );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_lostwrld = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );
/*TODO*///		ROM_LOAD_EVEN( "lw-11c.14f",    0x00000, 0x20000, 0x67e42546 );
/*TODO*///		ROM_LOAD_ODD ( "lw-15c.14g",    0x00000, 0x20000, 0x402e2a46 );
/*TODO*///		ROM_LOAD_EVEN( "lw-10c.13f",    0x40000, 0x20000, 0xc46479d7 );
/*TODO*///		ROM_LOAD_ODD ( "lw-14c.13g",    0x40000, 0x20000, 0x97670f4a );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "lw-07",         0x80000, 0x80000, 0xfd252a26 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "lw-06",         0x000000, 0x80000, 0x5b9edffc );
/*TODO*///		ROM_LOAD( "lw-05",         0x080000, 0x80000, 0xe4552fd7 );
/*TODO*///		ROM_LOAD( "lw-02",         0x100000, 0x80000, 0x43e6c5c8 );
/*TODO*///		ROM_LOAD( "lw-01",         0x180000, 0x80000, 0x0318f298 );
/*TODO*///		ROM_LOAD( "lw-13",         0x200000, 0x80000, 0x8e058ef5 );
/*TODO*///		ROM_LOAD( "lw-12",         0x280000, 0x80000, 0x8e6a832b );
/*TODO*///		ROM_LOAD( "lw-09",         0x300000, 0x80000, 0x899cb4ad );
/*TODO*///		ROM_LOAD( "lw-08",         0x380000, 0x80000, 0x25a8e43c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU */
/*TODO*///		ROM_LOAD( "lwu00",         0x00000, 0x08000, 0x59df2a63 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "lw-03.14c",     0x00000, 0x20000, 0xce2159e7 );
/*TODO*///		ROM_LOAD( "lw-04.13c",     0x20000, 0x20000, 0x39305536 );
/*TODO*///	ROM_END(); }}; 
	
	static RomLoadPtr rom_ghouls = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( CODE_SIZE, REGION_CPU1 );
		ROM_LOAD_EVEN( "ghl29.bin",    0x00000, 0x20000, 0x166a58a2 );
		ROM_LOAD_ODD ( "ghl30.bin",    0x00000, 0x20000, 0x7ac8407a );
		ROM_LOAD_EVEN( "ghl27.bin",    0x40000, 0x20000, 0xf734b2be );
		ROM_LOAD_ODD ( "ghl28.bin",    0x40000, 0x20000, 0x03d3e714 );
		ROM_LOAD_WIDE( "ghl17.bin",    0x80000, 0x80000, 0x3ea1b0f2 );
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD         ( "ghl6.bin",     0x000000, 0x80000, 0x4ba90b59 );
		ROM_LOAD_GFX_EVEN( "ghl11.bin",    0x080000, 0x10000, 0x37c9b6c6 );
		ROM_LOAD_GFX_ODD ( "ghl20.bin",    0x080000, 0x10000, 0x2f1345b4 );
		ROM_LOAD_GFX_EVEN( "ghl12.bin",    0x0a0000, 0x10000, 0xda088d61 );
		ROM_LOAD_GFX_ODD ( "ghl21.bin",    0x0a0000, 0x10000, 0x17e11df0 );
		ROM_LOAD         ( "ghl5.bin",     0x100000, 0x80000, 0x0ba9c0b0 );
		ROM_LOAD_GFX_EVEN( "ghl09.bin",    0x180000, 0x10000, 0xae24bb19 );
		ROM_LOAD_GFX_ODD ( "ghl18.bin",    0x180000, 0x10000, 0xd34e271a );
		ROM_LOAD_GFX_EVEN( "ghl10.bin",    0x1a0000, 0x10000, 0xbcc0f28c );
		ROM_LOAD_GFX_ODD ( "ghl19.bin",    0x1a0000, 0x10000, 0x2a40166a );
		ROM_LOAD         ( "ghl8.bin",     0x200000, 0x80000, 0x4bdee9de );
		ROM_LOAD_GFX_EVEN( "ghl15.bin",    0x280000, 0x10000, 0x3c2a212a );
		ROM_LOAD_GFX_ODD ( "ghl24.bin",    0x280000, 0x10000, 0x889aac05 );
		ROM_LOAD_GFX_EVEN( "ghl16.bin",    0x2a0000, 0x10000, 0xf187ba1c );
		ROM_LOAD_GFX_ODD ( "ghl25.bin",    0x2a0000, 0x10000, 0x29f79c78 );
		ROM_LOAD         ( "ghl7.bin",     0x300000, 0x80000, 0x5d760ab9 );
		ROM_LOAD_GFX_EVEN( "ghl13.bin",    0x380000, 0x10000, 0x3f70dd37 );
		ROM_LOAD_GFX_ODD ( "ghl22.bin",    0x380000, 0x10000, 0x7e69e2e6 );
		ROM_LOAD_GFX_EVEN( "ghl14.bin",    0x3a0000, 0x10000, 0x20f85c03 );
		ROM_LOAD_GFX_ODD ( "ghl23.bin",    0x3a0000, 0x10000, 0x8426144b );
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ghl26.bin",     0x00000, 0x08000, 0x3692f6e5 );
		ROM_CONTINUE(              0x10000, 0x08000 );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_ghoulsu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );
/*TODO*///		ROM_LOAD_EVEN( "dmu29",        0x00000, 0x20000, 0x334d85b2 );
/*TODO*///		ROM_LOAD_ODD ( "dmu30",        0x00000, 0x20000, 0xcee8ceb5 );
/*TODO*///		ROM_LOAD_EVEN( "dmu27",        0x40000, 0x20000, 0x4a524140 );
/*TODO*///		ROM_LOAD_ODD ( "dmu28",        0x40000, 0x20000, 0x94aae205 );
/*TODO*///		ROM_LOAD_WIDE( "ghl17.bin",    0x80000, 0x80000, 0x3ea1b0f2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "ghl6.bin",     0x000000, 0x80000, 0x4ba90b59 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl11.bin",    0x080000, 0x10000, 0x37c9b6c6 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl20.bin",    0x080000, 0x10000, 0x2f1345b4 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl12.bin",    0x0a0000, 0x10000, 0xda088d61 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl21.bin",    0x0a0000, 0x10000, 0x17e11df0 )
/*TODO*///		ROM_LOAD         ( "ghl5.bin",     0x100000, 0x80000, 0x0ba9c0b0 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl09.bin",    0x180000, 0x10000, 0xae24bb19 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl18.bin",    0x180000, 0x10000, 0xd34e271a )
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl10.bin",    0x1a0000, 0x10000, 0xbcc0f28c )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl19.bin",    0x1a0000, 0x10000, 0x2a40166a )
/*TODO*///		ROM_LOAD         ( "ghl8.bin",     0x200000, 0x80000, 0x4bdee9de );
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl15.bin",    0x280000, 0x10000, 0x3c2a212a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl24.bin",    0x280000, 0x10000, 0x889aac05 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl16.bin",    0x2a0000, 0x10000, 0xf187ba1c )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl25.bin",    0x2a0000, 0x10000, 0x29f79c78 )
/*TODO*///		ROM_LOAD         ( "ghl7.bin",     0x300000, 0x80000, 0x5d760ab9 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl13.bin",    0x380000, 0x10000, 0x3f70dd37 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl22.bin",    0x380000, 0x10000, 0x7e69e2e6 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "ghl14.bin",    0x3a0000, 0x10000, 0x20f85c03 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "ghl23.bin",    0x3a0000, 0x10000, 0x8426144b )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU */
/*TODO*///		ROM_LOAD( "ghl26.bin",     0x00000, 0x08000, 0x3692f6e5 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	ROM_END(); }}; 
	
	static RomLoadPtr rom_ghoulsj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( CODE_SIZE, REGION_CPU1 );
		ROM_LOAD_EVEN( "ghlj29.bin",   0x00000, 0x20000, 0x82fd1798 );
		ROM_LOAD_ODD ( "ghlj30.bin",   0x00000, 0x20000, 0x35366ccc );
		ROM_LOAD_EVEN( "ghlj27.bin",   0x40000, 0x20000, 0xa17c170a );
		ROM_LOAD_ODD ( "ghlj28.bin",   0x40000, 0x20000, 0x6af0b391 );
		ROM_LOAD_WIDE( "ghl17.bin",    0x80000, 0x80000, 0x3ea1b0f2 );
	
		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD         ( "ghl6.bin",     0x000000, 0x80000, 0x4ba90b59 );
		ROM_LOAD_GFX_EVEN( "ghl11.bin",    0x080000, 0x10000, 0x37c9b6c6 );
		ROM_LOAD_GFX_ODD ( "ghl20.bin",    0x080000, 0x10000, 0x2f1345b4 );
		ROM_LOAD_GFX_EVEN( "ghl12.bin",    0x0a0000, 0x10000, 0xda088d61 );
		ROM_LOAD_GFX_ODD ( "ghl21.bin",    0x0a0000, 0x10000, 0x17e11df0 );
		ROM_LOAD         ( "ghl5.bin",     0x100000, 0x80000, 0x0ba9c0b0 );
		ROM_LOAD_GFX_EVEN( "ghl09.bin",    0x180000, 0x10000, 0xae24bb19 );
		ROM_LOAD_GFX_ODD ( "ghl18.bin",    0x180000, 0x10000, 0xd34e271a );
		ROM_LOAD_GFX_EVEN( "ghl10.bin",    0x1a0000, 0x10000, 0xbcc0f28c );
		ROM_LOAD_GFX_ODD ( "ghl19.bin",    0x1a0000, 0x10000, 0x2a40166a );
		ROM_LOAD         ( "ghl8.bin",     0x200000, 0x80000, 0x4bdee9de );
		ROM_LOAD_GFX_EVEN( "ghl15.bin",    0x280000, 0x10000, 0x3c2a212a );
		ROM_LOAD_GFX_ODD ( "ghl24.bin",    0x280000, 0x10000, 0x889aac05 );
		ROM_LOAD_GFX_EVEN( "ghl16.bin",    0x2a0000, 0x10000, 0xf187ba1c );
		ROM_LOAD_GFX_ODD ( "ghl25.bin",    0x2a0000, 0x10000, 0x29f79c78 );
		ROM_LOAD         ( "ghl7.bin",     0x300000, 0x80000, 0x5d760ab9 );
		ROM_LOAD_GFX_EVEN( "ghl13.bin",    0x380000, 0x10000, 0x3f70dd37 );
		ROM_LOAD_GFX_ODD ( "ghl22.bin",    0x380000, 0x10000, 0x7e69e2e6 );
		ROM_LOAD_GFX_EVEN( "ghl14.bin",    0x3a0000, 0x10000, 0x20f85c03 );
		ROM_LOAD_GFX_ODD ( "ghl23.bin",    0x3a0000, 0x10000, 0x8426144b );
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU */
		ROM_LOAD( "ghl26.bin",     0x00000, 0x08000, 0x3692f6e5 );
		ROM_CONTINUE(              0x10000, 0x08000 );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_strider = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "strider.30",   0x00000, 0x20000, 0xda997474 );
/*TODO*///		ROM_LOAD_ODD ( "strider.35",   0x00000, 0x20000, 0x5463aaa3 );
/*TODO*///		ROM_LOAD_EVEN( "strider.31",   0x40000, 0x20000, 0xd20786db );
/*TODO*///		ROM_LOAD_ODD ( "strider.36",   0x40000, 0x20000, 0x21aa2863 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "strider.32",   0x80000, 0x80000, 0x9b3cfc08 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "strider.02",   0x000000, 0x80000, 0x7705aa46 );
/*TODO*///		ROM_LOAD( "strider.01",   0x080000, 0x80000, 0xb7d04e8b );
/*TODO*///		ROM_LOAD( "strider.06",   0x100000, 0x80000, 0x4eee9aea );
/*TODO*///		ROM_LOAD( "strider.05",   0x180000, 0x80000, 0x005f000b );
/*TODO*///		ROM_LOAD( "strider.04",   0x200000, 0x80000, 0x5b18b722 );
/*TODO*///		ROM_LOAD( "strider.03",   0x280000, 0x80000, 0x6b4713b4 );
/*TODO*///		ROM_LOAD( "strider.08",   0x300000, 0x80000, 0x2d7f21e4 );
/*TODO*///		ROM_LOAD( "strider.07",   0x380000, 0x80000, 0xb9441519 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "strider.09",    0x00000, 0x08000, 0x2ed403bc );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "strider.18",   0x00000, 0x20000, 0x4386bc80 );
/*TODO*///		ROM_LOAD( "strider.19",   0x20000, 0x20000, 0x444536d7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_striderj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sthj23.bin",   0x00000, 0x080000, 0x046e7b12 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "strider.32",   0x80000, 0x80000, 0x9b3cfc08 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "strider.02",   0x000000, 0x80000, 0x7705aa46 );
/*TODO*///		ROM_LOAD( "strider.01",   0x080000, 0x80000, 0xb7d04e8b );
/*TODO*///		ROM_LOAD( "strider.06",   0x100000, 0x80000, 0x4eee9aea );
/*TODO*///		ROM_LOAD( "strider.05",   0x180000, 0x80000, 0x005f000b );
/*TODO*///		ROM_LOAD( "strider.04",   0x200000, 0x80000, 0x5b18b722 );
/*TODO*///		ROM_LOAD( "strider.03",   0x280000, 0x80000, 0x6b4713b4 );
/*TODO*///		ROM_LOAD( "strider.08",   0x300000, 0x80000, 0x2d7f21e4 );
/*TODO*///		ROM_LOAD( "strider.07",   0x380000, 0x80000, 0xb9441519 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "strider.09",    0x00000, 0x08000, 0x2ed403bc );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "strider.18",   0x00000, 0x20000, 0x4386bc80 );
/*TODO*///		ROM_LOAD( "strider.19",   0x20000, 0x20000, 0x444536d7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_stridrja = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sth36.bin",   0x00000, 0x20000, 0x53c7b006 );
/*TODO*///		ROM_LOAD_ODD ( "sth42.bin",   0x00000, 0x20000, 0x4037f65f );
/*TODO*///		ROM_LOAD_EVEN( "sth37.bin",   0x40000, 0x20000, 0x80e8877d );
/*TODO*///		ROM_LOAD_ODD ( "sth43.bin",   0x40000, 0x20000, 0x6b3fa466 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "strider.32",   0x80000, 0x80000, 0x9b3cfc08 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "strider.02",   0x000000, 0x80000, 0x7705aa46 );
/*TODO*///		ROM_LOAD( "strider.01",   0x080000, 0x80000, 0xb7d04e8b );
/*TODO*///		ROM_LOAD( "strider.06",   0x100000, 0x80000, 0x4eee9aea );
/*TODO*///		ROM_LOAD( "strider.05",   0x180000, 0x80000, 0x005f000b );
/*TODO*///		ROM_LOAD( "strider.04",   0x200000, 0x80000, 0x5b18b722 );
/*TODO*///		ROM_LOAD( "strider.03",   0x280000, 0x80000, 0x6b4713b4 );
/*TODO*///		ROM_LOAD( "strider.08",   0x300000, 0x80000, 0x2d7f21e4 );
/*TODO*///		ROM_LOAD( "strider.07",   0x380000, 0x80000, 0xb9441519 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "strider.09",    0x00000, 0x08000, 0x2ed403bc );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "strider.18",   0x00000, 0x20000, 0x4386bc80 );
/*TODO*///		ROM_LOAD( "strider.19",   0x20000, 0x20000, 0x444536d7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_dwj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "36.bin",       0x00000, 0x20000, 0x1a516657 );
/*TODO*///		ROM_LOAD_ODD ( "42.bin",       0x00000, 0x20000, 0x12a290a0 );
/*TODO*///		ROM_LOAD_EVEN( "37.bin",       0x40000, 0x20000, 0x932fc943 );
/*TODO*///		ROM_LOAD_ODD ( "43.bin",       0x40000, 0x20000, 0x872ad76d );
/*TODO*///		ROM_LOAD_EVEN( "34.bin",       0x80000, 0x20000, 0x8f663d00 );
/*TODO*///		ROM_LOAD_ODD ( "40.bin",       0x80000, 0x20000, 0x1586dbf3 );
/*TODO*///		ROM_LOAD_EVEN( "35.bin",       0xc0000, 0x20000, 0x9db93d7a );
/*TODO*///		ROM_LOAD_ODD ( "41.bin",       0xc0000, 0x20000, 0x1aae69a4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "24.bin",       0x000000, 0x20000, 0xc6909b6f )
/*TODO*///		ROM_LOAD_GFX_ODD ( "17.bin",       0x000000, 0x20000, 0x2e2f8320 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "25.bin",       0x040000, 0x20000, 0x152ea74a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "18.bin",       0x040000, 0x20000, 0x1833f932 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "26.bin",       0x080000, 0x20000, 0x07fc714b )
/*TODO*///		ROM_LOAD_GFX_ODD ( "19.bin",       0x080000, 0x20000, 0x7114e5c6 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "27.bin",       0x0c0000, 0x20000, 0xa27e81fa )
/*TODO*///		ROM_LOAD_GFX_ODD ( "20.bin",       0x0c0000, 0x20000, 0x002796dc )
/*TODO*///		ROM_LOAD_GFX_EVEN( "09.bin",       0x100000, 0x20000, 0xc3e83c69 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "01.bin",       0x100000, 0x20000, 0x187b2886 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "10.bin",       0x140000, 0x20000, 0xff28f8d0 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "02.bin",       0x140000, 0x20000, 0xcc83c02f )
/*TODO*///		ROM_LOAD_GFX_EVEN( "11.bin",       0x180000, 0x20000, 0x29eaf490 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "03.bin",       0x180000, 0x20000, 0x7bf51337 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "12.bin",       0x1c0000, 0x20000, 0x38652339 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "04.bin",       0x1c0000, 0x20000, 0x4951bc0f )
/*TODO*///		ROM_LOAD_GFX_EVEN( "38.bin",       0x200000, 0x20000, 0xcd7923ed )
/*TODO*///		ROM_LOAD_GFX_ODD ( "32.bin",       0x200000, 0x20000, 0x21a0a453 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "39.bin",       0x240000, 0x20000, 0xbc09b360 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "33.bin",       0x240000, 0x20000, 0x89de1533 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "28.bin",       0x280000, 0x20000, 0xaf62bf07 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "21.bin",       0x280000, 0x20000, 0x523f462a )
/*TODO*///		ROM_LOAD_GFX_EVEN( "29.bin",       0x2c0000, 0x20000, 0x6b41f82d )
/*TODO*///		ROM_LOAD_GFX_ODD ( "22.bin",       0x2c0000, 0x20000, 0x52145369 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "13.bin",       0x300000, 0x20000, 0x0273d87d )
/*TODO*///		ROM_LOAD_GFX_ODD ( "05.bin",       0x300000, 0x20000, 0x339378b8 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "14.bin",       0x340000, 0x20000, 0x18fb232c )
/*TODO*///		ROM_LOAD_GFX_ODD ( "06.bin",       0x340000, 0x20000, 0x6f9edd75 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "15.bin",       0x380000, 0x20000, 0xd36cdb91 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "07.bin",       0x380000, 0x20000, 0xe04af054 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "16.bin",       0x3c0000, 0x20000, 0x381608ae )
/*TODO*///		ROM_LOAD_GFX_ODD ( "08.bin",       0x3c0000, 0x20000, 0xb475d4e9 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "23.bin",        0x00000, 0x08000, 0xb3b79d4f );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "30.bin",       0x00000, 0x20000, 0x7e5f6cb4 );
/*TODO*///		ROM_LOAD( "31.bin",       0x20000, 0x20000, 0x4a30c737 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_willow = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "wlu_30.rom",   0x00000, 0x20000, 0xd604dbb1 );
/*TODO*///		ROM_LOAD_ODD ( "wlu_35.rom",   0x00000, 0x20000, 0xdaee72fe );
/*TODO*///		ROM_LOAD_EVEN( "wlu_31.rom",   0x40000, 0x20000, 0x0eb48a83 );
/*TODO*///		ROM_LOAD_ODD ( "wlu_36.rom",   0x40000, 0x20000, 0x36100209 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "wl_32.rom",    0x80000, 0x80000, 0xdfd9f643 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "wl_gfx1.rom",  0x000000, 0x80000, 0xc6f2abce );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_20.rom",    0x080000, 0x20000, 0x84992350 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_10.rom",    0x080000, 0x20000, 0xb87b5a36 )
/*TODO*///		ROM_LOAD         ( "wl_gfx5.rom",  0x100000, 0x80000, 0xafa74b73 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_24.rom",    0x180000, 0x20000, 0x6f0adee5 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_14.rom",    0x180000, 0x20000, 0x9cf3027d )
/*TODO*///		ROM_LOAD         ( "wl_gfx3.rom",  0x200000, 0x80000, 0x4aa4c6d3 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_22.rom",    0x280000, 0x20000, 0xfd3f89f0 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_12.rom",    0x280000, 0x20000, 0x7da49d69 )
/*TODO*///		ROM_LOAD         ( "wl_gfx7.rom",  0x300000, 0x80000, 0x12a0dc0b );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_26.rom",    0x380000, 0x20000, 0xf09c8ecf )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_16.rom",    0x380000, 0x20000, 0xe35407aa )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "wl_09.rom",     0x00000, 0x08000, 0xf6b3d060 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "wl_18.rom",    0x00000, 0x20000, 0xbde23d4d );
/*TODO*///		ROM_LOAD( "wl_19.rom",    0x20000, 0x20000, 0x683898f5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_willowj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "wl36.bin",     0x00000, 0x20000, 0x2b0d7cbc );
/*TODO*///		ROM_LOAD_ODD ( "wl42.bin",     0x00000, 0x20000, 0x1ac39615 );
/*TODO*///		ROM_LOAD_EVEN( "wl37.bin",     0x40000, 0x20000, 0x30a717fa );
/*TODO*///		ROM_LOAD_ODD ( "wl43.bin",     0x40000, 0x20000, 0xd0dddc9e );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "wl_32.rom",    0x80000, 0x80000, 0xdfd9f643 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "wl_gfx1.rom",  0x000000, 0x80000, 0xc6f2abce );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_20.rom",    0x080000, 0x20000, 0x84992350 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_10.rom",    0x080000, 0x20000, 0xb87b5a36 )
/*TODO*///		ROM_LOAD         ( "wl_gfx5.rom",  0x100000, 0x80000, 0xafa74b73 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_24.rom",    0x180000, 0x20000, 0x6f0adee5 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_14.rom",    0x180000, 0x20000, 0x9cf3027d )
/*TODO*///		ROM_LOAD         ( "wl_gfx3.rom",  0x200000, 0x80000, 0x4aa4c6d3 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_22.rom",    0x280000, 0x20000, 0xfd3f89f0 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_12.rom",    0x280000, 0x20000, 0x7da49d69 )
/*TODO*///		ROM_LOAD         ( "wl_gfx7.rom",  0x300000, 0x80000, 0x12a0dc0b );
/*TODO*///		ROM_LOAD_GFX_EVEN( "wl_26.rom",    0x380000, 0x20000, 0xf09c8ecf )
/*TODO*///		ROM_LOAD_GFX_ODD ( "wl_16.rom",    0x380000, 0x20000, 0xe35407aa )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "wl_09.rom",     0x00000, 0x08000, 0xf6b3d060 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "wl_18.rom",    0x00000, 0x20000, 0xbde23d4d );
/*TODO*///		ROM_LOAD( "wl_19.rom",    0x20000, 0x20000, 0x683898f5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_unsquad = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "unsquad.30",   0x00000, 0x20000, 0x24d8f88d );
/*TODO*///		ROM_LOAD_ODD ( "unsquad.35",   0x00000, 0x20000, 0x8b954b59 );
/*TODO*///		ROM_LOAD_EVEN( "unsquad.31",   0x40000, 0x20000, 0x33e9694b );
/*TODO*///		ROM_LOAD_ODD ( "unsquad.36",   0x40000, 0x20000, 0x7cc8fb9e );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "unsquad.32",   0x80000, 0x80000, 0xae1d7fb0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "unsquad.01",   0x000000, 0x80000, 0x5965ca8d );
/*TODO*///		ROM_LOAD( "unsquad.05",   0x080000, 0x80000, 0xbf4575d8 );
/*TODO*///		ROM_LOAD( "unsquad.03",   0x100000, 0x80000, 0xac6db17d );
/*TODO*///		ROM_LOAD( "unsquad.07",   0x180000, 0x80000, 0xa02945f4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "unsquad.09",    0x00000, 0x08000, 0xf3dd1367 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "unsquad.18",   0x00000, 0x20000, 0x584b43a9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_area88 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "ar36.bin",     0x00000, 0x20000, 0x65030392 );
/*TODO*///		ROM_LOAD_ODD ( "ar42.bin",     0x00000, 0x20000, 0xc48170de );
/*TODO*///		ROM_LOAD_EVEN( "unsquad.31",   0x40000, 0x20000, 0x33e9694b );
/*TODO*///		ROM_LOAD_ODD ( "unsquad.36",   0x40000, 0x20000, 0x7cc8fb9e );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "unsquad.32",   0x80000, 0x80000, 0xae1d7fb0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "unsquad.01",   0x000000, 0x80000, 0x5965ca8d );
/*TODO*///		ROM_LOAD( "unsquad.05",   0x080000, 0x80000, 0xbf4575d8 );
/*TODO*///		ROM_LOAD( "unsquad.03",   0x100000, 0x80000, 0xac6db17d );
/*TODO*///		ROM_LOAD( "unsquad.07",   0x180000, 0x80000, 0xa02945f4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "unsquad.09",    0x00000, 0x08000, 0xf3dd1367 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x20000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "unsquad.18",   0x00000, 0x20000, 0x584b43a9 );
/*TODO*///	ROM_END(); }}; 
	
	static RomLoadPtr rom_ffight = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
		ROM_LOAD_EVEN( "ff30-36.bin",  0x00000, 0x20000, 0xf9a5ce83 );
		ROM_LOAD_ODD ( "ff35-42.bin",  0x00000, 0x20000, 0x65f11215 );
		ROM_LOAD_EVEN( "ff31-37.bin",  0x40000, 0x20000, 0xe1033784 );
		ROM_LOAD_ODD ( "ff36-43.bin",  0x40000, 0x20000, 0x995e968a );
		ROM_LOAD_WIDE_SWAP( "ff32-32m.bin", 0x80000, 0x80000, 0xc747696e );
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD( "ff01-01m.bin", 0x000000, 0x80000, 0x0b605e44 );
		ROM_LOAD( "ff05-05m.bin", 0x080000, 0x80000, 0x9c284108 );
		ROM_LOAD( "ff03-03m.bin", 0x100000, 0x80000, 0x52291cd2 );
		ROM_LOAD( "ff07-07m.bin", 0x180000, 0x80000, 0xa7584dfb );
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
		ROM_LOAD( "ff09-09.bin",   0x00000, 0x08000, 0xb8367eb5 );
		ROM_CONTINUE(              0x10000, 0x08000 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "ff18-18.bin",  0x00000, 0x20000, 0x375c66e7 );
		ROM_LOAD( "ff19-19.bin",  0x20000, 0x20000, 0x1ef137f9 );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_ffightu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "36",           0x00000, 0x20000, 0xe2a48af9 );
/*TODO*///		ROM_LOAD_ODD ( "42",           0x00000, 0x20000, 0xf4bb480e );
/*TODO*///		ROM_LOAD_EVEN( "37",           0x40000, 0x20000, 0xc371c667 );
/*TODO*///		ROM_LOAD_ODD ( "43",           0x40000, 0x20000, 0x2f5771f9 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ff32-32m.bin", 0x80000, 0x80000, 0xc747696e );
/*TODO*///	
/*TODO*///		/* Note: the gfx ROMs were missing from this set. I used the ones from */
/*TODO*///		/* the World version, assuming the if the scantily clad woman shouldn't */
/*TODO*///		/* be seen in Europe, it shouldn't be seen in the USA as well. */
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ff01-01m.bin", 0x000000, 0x80000, 0x0b605e44 );
/*TODO*///		ROM_LOAD( "ff05-05m.bin", 0x080000, 0x80000, 0x9c284108 );
/*TODO*///		ROM_LOAD( "ff03-03m.bin", 0x100000, 0x80000, 0x52291cd2 );
/*TODO*///		ROM_LOAD( "ff07-07m.bin", 0x180000, 0x80000, 0xa7584dfb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ff09-09.bin",   0x00000, 0x08000, 0xb8367eb5 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ff18-18.bin",  0x00000, 0x20000, 0x375c66e7 );
/*TODO*///		ROM_LOAD( "ff19-19.bin",  0x20000, 0x20000, 0x1ef137f9 );
/*TODO*///	ROM_END(); }}; 
	
	static RomLoadPtr rom_ffightj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
		ROM_LOAD_EVEN( "ff30-36.bin",  0x00000, 0x20000, 0xf9a5ce83 );
		ROM_LOAD_ODD ( "ff35-42.bin",  0x00000, 0x20000, 0x65f11215 );
		ROM_LOAD_EVEN( "ff31-37.bin",  0x40000, 0x20000, 0xe1033784 );
		ROM_LOAD_ODD ( "ff43.bin",     0x40000, 0x20000, 0xb6dee1c3 );
		ROM_LOAD_WIDE_SWAP( "ff32-32m.bin", 0x80000, 0x80000, 0xc747696e );
	
		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
		ROM_LOAD_GFX_EVEN( "ff24.bin",     0x000000, 0x20000, 0xa1ab607a );
		ROM_LOAD_GFX_ODD ( "ff17.bin",     0x000000, 0x20000, 0x2dc18cf4 );
		ROM_LOAD_GFX_EVEN( "ff25.bin",     0x040000, 0x20000, 0x6e8181ea );
		ROM_LOAD_GFX_ODD ( "ff18.bin",     0x040000, 0x20000, 0xb19ede59 );
		ROM_LOAD_GFX_EVEN( "ff09.bin",     0x080000, 0x20000, 0x5b116d0d );
		ROM_LOAD_GFX_ODD ( "ff01.bin",     0x080000, 0x20000, 0x815b1797 );
		ROM_LOAD_GFX_EVEN( "ff10.bin",     0x0c0000, 0x20000, 0x624a924a );
		ROM_LOAD_GFX_ODD ( "ff02.bin",     0x0c0000, 0x20000, 0x5d91f694 );
		ROM_LOAD_GFX_EVEN( "ff38.bin",     0x100000, 0x20000, 0x6535a57f );
		ROM_LOAD_GFX_ODD ( "ff32.bin",     0x100000, 0x20000, 0xc8bc4a57 );
		ROM_LOAD_GFX_EVEN( "ff39.bin",     0x140000, 0x20000, 0x9416b477 );
		ROM_LOAD_GFX_ODD ( "ff33.bin",     0x140000, 0x20000, 0x7369fa07 );
		ROM_LOAD_GFX_EVEN( "ff13.bin",     0x180000, 0x20000, 0x8721a7da );
		ROM_LOAD_GFX_ODD ( "ff05.bin",     0x180000, 0x20000, 0xd0fcd4b5 );
		ROM_LOAD_GFX_EVEN( "ff14.bin",     0x1c0000, 0x20000, 0x0a2e9101 );
		ROM_LOAD_GFX_ODD ( "ff06.bin",     0x1c0000, 0x20000, 0x1c18f042 );
	
		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
		ROM_LOAD( "ff09-09.bin",   0x00000, 0x08000, 0xb8367eb5 );
		ROM_CONTINUE(              0x10000, 0x08000 );
	
		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
		ROM_LOAD( "ff18-18.bin",  0x00000, 0x20000, 0x375c66e7 );
		ROM_LOAD( "ff19-19.bin",  0x20000, 0x20000, 0x1ef137f9 );
	ROM_END(); }}; 
	
/*TODO*///	static RomLoadPtr rom_1941 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "41e_30.rom",   0x00000, 0x20000, 0x9deb1e75 );
/*TODO*///		ROM_LOAD_ODD ( "41e_35.rom",   0x00000, 0x20000, 0xd63942b3 );
/*TODO*///		ROM_LOAD_EVEN( "41e_31.rom",   0x40000, 0x20000, 0xdf201112 );
/*TODO*///		ROM_LOAD_ODD ( "41e_36.rom",   0x40000, 0x20000, 0x816a818f );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "41_32.rom",    0x80000, 0x80000, 0x4e9648ca );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "41_gfx1.rom",  0x000000, 0x80000, 0xff77985a );
/*TODO*///		ROM_LOAD( "41_gfx5.rom",  0x080000, 0x80000, 0x01d1cb11 );
/*TODO*///		ROM_LOAD( "41_gfx3.rom",  0x100000, 0x80000, 0x983be58f );
/*TODO*///		ROM_LOAD( "41_gfx7.rom",  0x180000, 0x80000, 0xaeaa3509 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "41_09.rom",     0x00000, 0x08000, 0x0f9d8527 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "41_18.rom",    0x00000, 0x20000, 0xd1f15aeb );
/*TODO*///		ROM_LOAD( "41_19.rom",    0x20000, 0x20000, 0x15aec3a6 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_1941j = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "4136.bin",     0x00000, 0x20000, 0x7fbd42ab );
/*TODO*///		ROM_LOAD_ODD ( "4142.bin",     0x00000, 0x20000, 0xc7781f89 );
/*TODO*///		ROM_LOAD_EVEN( "4137.bin",     0x40000, 0x20000, 0xc6464b0b );
/*TODO*///		ROM_LOAD_ODD ( "4143.bin",     0x40000, 0x20000, 0x440fc0b5 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "41_32.rom",    0x80000, 0x80000, 0x4e9648ca );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "41_gfx1.rom",  0x000000, 0x80000, 0xff77985a );
/*TODO*///		ROM_LOAD( "41_gfx5.rom",  0x080000, 0x80000, 0x01d1cb11 );
/*TODO*///		ROM_LOAD( "41_gfx3.rom",  0x100000, 0x80000, 0x983be58f );
/*TODO*///		ROM_LOAD( "41_gfx7.rom",  0x180000, 0x80000, 0xaeaa3509 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "41_09.rom",     0x00000, 0x08000, 0x0f9d8527 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "41_18.rom",    0x00000, 0x20000, 0xd1f15aeb );
/*TODO*///		ROM_LOAD( "41_19.rom",    0x20000, 0x20000, 0x15aec3a6 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mercs = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "so2_30e.rom",  0x00000, 0x20000, 0xe17f9bf7 );
/*TODO*///		ROM_LOAD_ODD ( "so2_35e.rom",  0x00000, 0x20000, 0x78e63575 );
/*TODO*///		ROM_LOAD_EVEN( "so2_31e.rom",  0x40000, 0x20000, 0x51204d36 );
/*TODO*///		ROM_LOAD_ODD ( "so2_36e.rom",  0x40000, 0x20000, 0x9cfba8b4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "so2_32.rom",   0x80000, 0x80000, 0x2eb5cf0c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "so2_gfx2.rom", 0x000000, 0x80000, 0x597c2875 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_20.rom",   0x080000, 0x20000, 0x8ca751a3 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_10.rom",   0x080000, 0x20000, 0xe9f569fd )
/*TODO*///		ROM_LOAD         ( "so2_gfx6.rom", 0x100000, 0x80000, 0xaa6102af );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_24.rom",   0x180000, 0x20000, 0x3f254efe )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_14.rom",   0x180000, 0x20000, 0xf5a8905e )
/*TODO*///		ROM_LOAD         ( "so2_gfx4.rom", 0x200000, 0x80000, 0x912a9ca0 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_22.rom",   0x280000, 0x20000, 0xfce9a377 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_12.rom",   0x280000, 0x20000, 0xb7df8a06 )
/*TODO*///		ROM_LOAD         ( "so2_gfx8.rom", 0x300000, 0x80000, 0x839e6869 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_26.rom",   0x380000, 0x20000, 0xf3aa5a4a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_16.rom",   0x380000, 0x20000, 0xb43cd1a8 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "so2_09.rom",    0x00000, 0x08000, 0xd09d7c7a );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "so2_18.rom",   0x00000, 0x20000, 0xbbea1643 );
/*TODO*///		ROM_LOAD( "so2_19.rom",   0x20000, 0x20000, 0xac58aa71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mercsu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "so2_30e.rom",  0x00000, 0x20000, 0xe17f9bf7 );
/*TODO*///		ROM_LOAD_ODD ( "s02-35",       0x00000, 0x20000, 0x4477df61 );
/*TODO*///		ROM_LOAD_EVEN( "so2_31e.rom",  0x40000, 0x20000, 0x51204d36 );
/*TODO*///		ROM_LOAD_ODD ( "so2_36e.rom",  0x40000, 0x20000, 0x9cfba8b4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "so2_32.rom",   0x80000, 0x80000, 0x2eb5cf0c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "so2_gfx2.rom", 0x000000, 0x80000, 0x597c2875 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_20.rom",   0x080000, 0x20000, 0x8ca751a3 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_10.rom",   0x080000, 0x20000, 0xe9f569fd )
/*TODO*///		ROM_LOAD         ( "so2_gfx6.rom", 0x100000, 0x80000, 0xaa6102af );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_24.rom",   0x180000, 0x20000, 0x3f254efe )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_14.rom",   0x180000, 0x20000, 0xf5a8905e )
/*TODO*///		ROM_LOAD         ( "so2_gfx4.rom", 0x200000, 0x80000, 0x912a9ca0 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_22.rom",   0x280000, 0x20000, 0xfce9a377 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_12.rom",   0x280000, 0x20000, 0xb7df8a06 )
/*TODO*///		ROM_LOAD         ( "so2_gfx8.rom", 0x300000, 0x80000, 0x839e6869 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_26.rom",   0x380000, 0x20000, 0xf3aa5a4a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_16.rom",   0x380000, 0x20000, 0xb43cd1a8 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "so2_09.rom",    0x00000, 0x08000, 0xd09d7c7a );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "so2_18.rom",   0x00000, 0x20000, 0xbbea1643 );
/*TODO*///		ROM_LOAD( "so2_19.rom",   0x20000, 0x20000, 0xac58aa71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mercsj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "so2_30e.rom",  0x00000, 0x20000, 0xe17f9bf7 );
/*TODO*///		ROM_LOAD_ODD ( "so2_42.bin",   0x00000, 0x20000, 0x2c3884c6 );
/*TODO*///		ROM_LOAD_EVEN( "so2_31e.rom",  0x40000, 0x20000, 0x51204d36 );
/*TODO*///		ROM_LOAD_ODD ( "so2_36e.rom",  0x40000, 0x20000, 0x9cfba8b4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "so2_32.rom",   0x80000, 0x80000, 0x2eb5cf0c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD         ( "so2_gfx2.rom", 0x000000, 0x80000, 0x597c2875 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_20.rom",   0x080000, 0x20000, 0x8ca751a3 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_10.rom",   0x080000, 0x20000, 0xe9f569fd )
/*TODO*///		ROM_LOAD         ( "so2_gfx6.rom", 0x100000, 0x80000, 0xaa6102af );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_24.rom",   0x180000, 0x20000, 0x3f254efe )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_14.rom",   0x180000, 0x20000, 0xf5a8905e )
/*TODO*///		ROM_LOAD         ( "so2_gfx4.rom", 0x200000, 0x80000, 0x912a9ca0 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_22.rom",   0x280000, 0x20000, 0xfce9a377 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_12.rom",   0x280000, 0x20000, 0xb7df8a06 )
/*TODO*///		ROM_LOAD         ( "so2_gfx8.rom", 0x300000, 0x80000, 0x839e6869 );
/*TODO*///		ROM_LOAD_GFX_EVEN( "so2_26.rom",   0x380000, 0x20000, 0xf3aa5a4a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "so2_16.rom",   0x380000, 0x20000, 0xb43cd1a8 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "so2_09.rom",    0x00000, 0x08000, 0xd09d7c7a );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "so2_18.rom",   0x00000, 0x20000, 0xbbea1643 );
/*TODO*///		ROM_LOAD( "so2_19.rom",   0x20000, 0x20000, 0xac58aa71 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mtwins = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "che_30.rom",   0x00000, 0x20000, 0x9a2a2db1 );
/*TODO*///		ROM_LOAD_ODD ( "che_35.rom",   0x00000, 0x20000, 0xa7f96b02 );
/*TODO*///		ROM_LOAD_EVEN( "che_31.rom",   0x40000, 0x20000, 0xbbff8a99 );
/*TODO*///		ROM_LOAD_ODD ( "che_36.rom",   0x40000, 0x20000, 0x0fa00c39 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ch_32.rom",    0x80000, 0x80000, 0x9b70bd41 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ch_gfx1.rom",  0x000000, 0x80000, 0xf33ca9d4 );
/*TODO*///		ROM_LOAD( "ch_gfx5.rom",  0x080000, 0x80000, 0x4ec75f15 );
/*TODO*///		ROM_LOAD( "ch_gfx3.rom",  0x100000, 0x80000, 0x0ba2047f );
/*TODO*///		ROM_LOAD( "ch_gfx7.rom",  0x180000, 0x80000, 0xd85d00d6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ch_09.rom",     0x00000, 0x08000, 0x4d4255b7 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ch_18.rom",    0x00000, 0x20000, 0xf909e8de );
/*TODO*///		ROM_LOAD( "ch_19.rom",    0x20000, 0x20000, 0xfc158cf7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_chikij = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "chj36a.bin",   0x00000, 0x20000, 0xec1328d8 );
/*TODO*///		ROM_LOAD_ODD ( "chj42a.bin",   0x00000, 0x20000, 0x4ae13503 );
/*TODO*///		ROM_LOAD_EVEN( "chj37a.bin",   0x40000, 0x20000, 0x46d2cf7b );
/*TODO*///		ROM_LOAD_ODD ( "chj43a.bin",   0x40000, 0x20000, 0x8d387fe8 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ch_32.rom",    0x80000, 0x80000, 0x9b70bd41 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ch_gfx1.rom",  0x000000, 0x80000, 0xf33ca9d4 );
/*TODO*///		ROM_LOAD( "ch_gfx5.rom",  0x080000, 0x80000, 0x4ec75f15 );
/*TODO*///		ROM_LOAD( "ch_gfx3.rom",  0x100000, 0x80000, 0x0ba2047f );
/*TODO*///		ROM_LOAD( "ch_gfx7.rom",  0x180000, 0x80000, 0xd85d00d6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ch_09.rom",     0x00000, 0x08000, 0x4d4255b7 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ch_18.rom",    0x00000, 0x20000, 0xf909e8de );
/*TODO*///		ROM_LOAD( "ch_19.rom",    0x20000, 0x20000, 0xfc158cf7 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_msword = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "mse_30.rom",   0x00000, 0x20000, 0x03fc8dbc );
/*TODO*///		ROM_LOAD_ODD ( "mse_35.rom",   0x00000, 0x20000, 0xd5bf66cd );
/*TODO*///		ROM_LOAD_EVEN( "mse_31.rom",   0x40000, 0x20000, 0x30332bcf );
/*TODO*///		ROM_LOAD_ODD ( "mse_36.rom",   0x40000, 0x20000, 0x8f7d6ce9 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ms_32.rom",    0x80000, 0x80000, 0x2475ddfc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ms_gfx1.rom",  0x000000, 0x80000, 0x0d2bbe00 );
/*TODO*///		ROM_LOAD( "ms_gfx5.rom",  0x080000, 0x80000, 0xc00fe7e2 );
/*TODO*///		ROM_LOAD( "ms_gfx3.rom",  0x100000, 0x80000, 0x3a1a5bf4 );
/*TODO*///		ROM_LOAD( "ms_gfx7.rom",  0x180000, 0x80000, 0x4ccacac5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ms_9.rom",      0x00000, 0x08000, 0x57b29519 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ms_18.rom",    0x00000, 0x20000, 0xfb64e90d );
/*TODO*///		ROM_LOAD( "ms_19.rom",    0x20000, 0x20000, 0x74f892b9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mswordu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "msu30",   0x00000, 0x20000, 0xd963c816 );
/*TODO*///		ROM_LOAD_ODD ( "msu35",   0x00000, 0x20000, 0x72f179b3 );
/*TODO*///		ROM_LOAD_EVEN( "msu31",   0x40000, 0x20000, 0x20cd7904 );
/*TODO*///		ROM_LOAD_ODD ( "msu36",   0x40000, 0x20000, 0xbf88c080 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ms_32.rom",    0x80000, 0x80000, 0x2475ddfc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ms_gfx1.rom",  0x000000, 0x80000, 0x0d2bbe00 );
/*TODO*///		ROM_LOAD( "ms_gfx5.rom",  0x080000, 0x80000, 0xc00fe7e2 );
/*TODO*///		ROM_LOAD( "ms_gfx3.rom",  0x100000, 0x80000, 0x3a1a5bf4 );
/*TODO*///		ROM_LOAD( "ms_gfx7.rom",  0x180000, 0x80000, 0x4ccacac5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ms_9.rom",      0x00000, 0x08000, 0x57b29519 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ms_18.rom",    0x00000, 0x20000, 0xfb64e90d );
/*TODO*///		ROM_LOAD( "ms_19.rom",    0x20000, 0x20000, 0x74f892b9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mswordj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "msj_30.rom",   0x00000, 0x20000, 0x04f0ef50 );
/*TODO*///		ROM_LOAD_ODD ( "msj_35.rom",   0x00000, 0x20000, 0x9fcbb9cd );
/*TODO*///		ROM_LOAD_EVEN( "msj_31.rom",   0x40000, 0x20000, 0x6c060d70 );
/*TODO*///		ROM_LOAD_ODD ( "msj_36.rom",   0x40000, 0x20000, 0xaec77787 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ms_32.rom",    0x80000, 0x80000, 0x2475ddfc );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ms_gfx1.rom",  0x000000, 0x80000, 0x0d2bbe00 );
/*TODO*///		ROM_LOAD( "ms_gfx5.rom",  0x080000, 0x80000, 0xc00fe7e2 );
/*TODO*///		ROM_LOAD( "ms_gfx3.rom",  0x100000, 0x80000, 0x3a1a5bf4 );
/*TODO*///		ROM_LOAD( "ms_gfx7.rom",  0x180000, 0x80000, 0x4ccacac5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ms_9.rom",      0x00000, 0x08000, 0x57b29519 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ms_18.rom",    0x00000, 0x20000, 0xfb64e90d );
/*TODO*///		ROM_LOAD( "ms_19.rom",    0x20000, 0x20000, 0x74f892b9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_cawing = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "cae_30a.rom",  0x00000, 0x20000, 0x91fceacd );
/*TODO*///		ROM_LOAD_ODD ( "cae_35a.rom",  0x00000, 0x20000, 0x3ef03083 );
/*TODO*///		ROM_LOAD_EVEN( "cae_31a.rom",  0x40000, 0x20000, 0xe5b75caf );
/*TODO*///		ROM_LOAD_ODD ( "cae_36a.rom",  0x40000, 0x20000, 0xc73fd713 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ca_32.rom", 0x80000, 0x80000, 0x0c4837d4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ca_gfx1.rom",  0x000000, 0x80000, 0x4d0620fd );
/*TODO*///		ROM_LOAD( "ca_gfx5.rom",  0x080000, 0x80000, 0x66d4cc37 );
/*TODO*///		ROM_LOAD( "ca_gfx3.rom",  0x100000, 0x80000, 0x0b0341c3 );
/*TODO*///		ROM_LOAD( "ca_gfx7.rom",  0x180000, 0x80000, 0xb6f896f2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ca_9.rom",      0x00000, 0x08000, 0x96fe7485 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ca_18.rom",    0x00000, 0x20000, 0x4a613a2c );
/*TODO*///		ROM_LOAD( "ca_19.rom",    0x20000, 0x20000, 0x74584493 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_cawingj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "cae_30a.rom",  0x00000, 0x20000, 0x91fceacd );
/*TODO*///		ROM_LOAD_ODD ( "caj42a.bin",   0x00000, 0x20000, 0x039f8362 );
/*TODO*///		ROM_LOAD_EVEN( "cae_31a.rom",  0x40000, 0x20000, 0xe5b75caf );
/*TODO*///		ROM_LOAD_ODD ( "cae_36a.rom",  0x40000, 0x20000, 0xc73fd713 );
/*TODO*///		ROM_LOAD_EVEN( "caj34.bin",    0x80000, 0x20000, 0x51ea57f4 );
/*TODO*///		ROM_LOAD_ODD ( "caj40.bin",    0x80000, 0x20000, 0x2ab71ae1 );
/*TODO*///		ROM_LOAD_EVEN( "caj35.bin",    0xc0000, 0x20000, 0x01d71973 );
/*TODO*///		ROM_LOAD_ODD ( "caj41.bin",    0xc0000, 0x20000, 0x3a43b538 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj24.bin",    0x000000, 0x20000, 0xe356aad7 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj17.bin",    0x000000, 0x20000, 0x540f2fd8 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj25.bin",    0x040000, 0x20000, 0xcdd0204d )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj18.bin",    0x040000, 0x20000, 0x29c1d4b1 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj09.bin",    0x080000, 0x20000, 0x41b0f9a6 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj01.bin",    0x080000, 0x20000, 0x1002d0b8 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj10.bin",    0x0c0000, 0x20000, 0xbf8a5f52 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj02.bin",    0x0c0000, 0x20000, 0x125b018d )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj38.bin",    0x100000, 0x20000, 0x2464d4ab )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj32.bin",    0x100000, 0x20000, 0x9b5836b3 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj39.bin",    0x140000, 0x20000, 0xeea23b67 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj33.bin",    0x140000, 0x20000, 0xdde3891f )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj13.bin",    0x180000, 0x20000, 0x6f3948b2 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj05.bin",    0x180000, 0x20000, 0x207373d7 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "caj14.bin",    0x1c0000, 0x20000, 0x8458e7d7 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "caj06.bin",    0x1c0000, 0x20000, 0xcf80e164 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "ca_9.rom",      0x00000, 0x08000, 0x96fe7485 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "ca_18.rom",    0x00000, 0x20000, 0x4a613a2c );
/*TODO*///		ROM_LOAD( "ca_19.rom",    0x20000, 0x20000, 0x74584493 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_nemo = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "nme_30a.rom",  0x00000, 0x20000, 0xd2c03e56 );
/*TODO*///		ROM_LOAD_ODD ( "nme_35a.rom",  0x00000, 0x20000, 0x5fd31661 );
/*TODO*///		ROM_LOAD_EVEN( "nme_31a.rom",  0x40000, 0x20000, 0xb2bd4f6f );
/*TODO*///		ROM_LOAD_ODD ( "nme_36a.rom",  0x40000, 0x20000, 0xee9450e3 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "nm_32.rom",    0x80000, 0x80000, 0xd6d1add3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "nm_gfx1.rom",  0x000000, 0x80000, 0x9e878024 );
/*TODO*///		ROM_LOAD( "nm_gfx5.rom",  0x080000, 0x80000, 0x487b8747 );
/*TODO*///		ROM_LOAD( "nm_gfx3.rom",  0x100000, 0x80000, 0xbb01e6b6 );
/*TODO*///		ROM_LOAD( "nm_gfx7.rom",  0x180000, 0x80000, 0x203dc8c6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "nm_09.rom",     0x00000, 0x08000, 0x0f4b0581 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "nm_18.rom",    0x00000, 0x20000, 0xbab333d4 );
/*TODO*///		ROM_LOAD( "nm_19.rom",    0x20000, 0x20000, 0x2650a0a8 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_nemoj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "nm36.bin",     0x00000, 0x20000, 0xdaeceabb );
/*TODO*///		ROM_LOAD_ODD ( "nm42.bin",     0x00000, 0x20000, 0x55024740 );
/*TODO*///		ROM_LOAD_EVEN( "nm37.bin",     0x40000, 0x20000, 0x619068b6 );
/*TODO*///		ROM_LOAD_ODD ( "nm43.bin",     0x40000, 0x20000, 0xa948a53b );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "nm_32.rom",    0x80000, 0x80000, 0xd6d1add3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "nm_gfx1.rom",  0x000000, 0x80000, 0x9e878024 );
/*TODO*///		ROM_LOAD( "nm_gfx5.rom",  0x080000, 0x80000, 0x487b8747 );
/*TODO*///		ROM_LOAD( "nm_gfx3.rom",  0x100000, 0x80000, 0xbb01e6b6 );
/*TODO*///		ROM_LOAD( "nm_gfx7.rom",  0x180000, 0x80000, 0x203dc8c6 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "nm_09.rom",     0x00000, 0x08000, 0x0f4b0581 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "nm_18.rom",    0x00000, 0x20000, 0xbab333d4 );
/*TODO*///		ROM_LOAD( "nm_19.rom",    0x20000, 0x20000, 0x2650a0a8 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2e_30b.rom",    0x00000, 0x20000, 0x57bd7051 );
/*TODO*///		ROM_LOAD_ODD ( "sf2e_37b.rom",    0x00000, 0x20000, 0x62691cdd );
/*TODO*///		ROM_LOAD_EVEN( "sf2e_31b.rom",    0x40000, 0x20000, 0xa673143d );
/*TODO*///		ROM_LOAD_ODD ( "sf2e_38b.rom",    0x40000, 0x20000, 0x4c2ccef7 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_28b.rom",     0x80000, 0x20000, 0x4009955e );
/*TODO*///		ROM_LOAD_ODD ( "sf2_35b.rom",     0x80000, 0x20000, 0x8c1f3994 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom",     0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom",     0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2a = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2u.30a",    0x00000, 0x20000, 0x08beb861 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.37a",    0x00000, 0x20000, 0xb7638d69 );
/*TODO*///		ROM_LOAD_EVEN( "sf2u.31a",    0x40000, 0x20000, 0x0d5394e0 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.38a",    0x40000, 0x20000, 0x42d6a79e );
/*TODO*///		ROM_LOAD_EVEN( "sf2u.28a",    0x80000, 0x20000, 0x387a175c );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.35a",    0x80000, 0x20000, 0xa1a5adcc );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom", 0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom", 0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2b = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2e_30b.rom",   0x00000, 0x20000, 0x57bd7051 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.37b",       0x00000, 0x20000, 0x4a54d479 );
/*TODO*///		ROM_LOAD_EVEN( "sf2e_31b.rom",   0x40000, 0x20000, 0xa673143d );
/*TODO*///		ROM_LOAD_ODD ( "sf2e_38b.rom",   0x40000, 0x20000, 0x4c2ccef7 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_28b.rom",    0x80000, 0x20000, 0x4009955e );
/*TODO*///		ROM_LOAD_ODD ( "sf2_35b.rom",    0x80000, 0x20000, 0x8c1f3994 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom",    0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom",    0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2e = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2u.30e",    0x00000, 0x20000, 0xf37cd088 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.37e",    0x00000, 0x20000, 0x6c61a513 );
/*TODO*///		ROM_LOAD_EVEN( "sf2u.31e",    0x40000, 0x20000, 0x7c4771b4 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.38e",    0x40000, 0x20000, 0xa4bd0cd9 );
/*TODO*///		ROM_LOAD_EVEN( "sf2u.28e",    0x80000, 0x20000, 0xe3b95625 );
/*TODO*///		ROM_LOAD_ODD ( "sf2u.35e",    0x80000, 0x20000, 0x3648769a );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom", 0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom", 0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2j = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2j30.bin",    0x00000, 0x20000, 0x79022b31 );
/*TODO*///		ROM_LOAD_ODD ( "sf2j37.bin",    0x00000, 0x20000, 0x516776ec );
/*TODO*///		ROM_LOAD_EVEN( "sf2j31.bin",    0x40000, 0x20000, 0xfe15cb39 );
/*TODO*///		ROM_LOAD_ODD ( "sf2j38.bin",    0x40000, 0x20000, 0x38614d70 );
/*TODO*///		ROM_LOAD_EVEN( "sf2j28.bin",    0x80000, 0x20000, 0xd283187a );
/*TODO*///		ROM_LOAD_ODD ( "sf2j35.bin",    0x80000, 0x20000, 0xd28158e4 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom",   0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom",   0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2jb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "sf2e_30b.rom",   0x00000, 0x20000, 0x57bd7051 );
/*TODO*///		ROM_LOAD_ODD ( "sf2j_37b.rom",   0x00000, 0x20000, 0x1e1f6844 );
/*TODO*///		ROM_LOAD_EVEN( "sf2e_31b.rom",   0x40000, 0x20000, 0xa673143d );
/*TODO*///		ROM_LOAD_ODD ( "sf2e_38b.rom",   0x40000, 0x20000, 0x4c2ccef7 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_28b.rom",    0x80000, 0x20000, 0x4009955e );
/*TODO*///		ROM_LOAD_ODD ( "sf2_35b.rom",    0x80000, 0x20000, 0x8c1f3994 );
/*TODO*///		ROM_LOAD_EVEN( "sf2_29b.rom",    0xc0000, 0x20000, 0xbb4af315 );
/*TODO*///		ROM_LOAD_ODD ( "sf2_36b.rom",    0xc0000, 0x20000, 0xc02a13eb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2gfx01.rom",       0x000000, 0x80000, 0xba529b4f );
/*TODO*///		ROM_LOAD( "sf2gfx10.rom",       0x080000, 0x80000, 0x14b84312 );
/*TODO*///		ROM_LOAD( "sf2gfx20.rom",       0x100000, 0x80000, 0xc1befaa8 );
/*TODO*///		ROM_LOAD( "sf2gfx02.rom",       0x180000, 0x80000, 0x22c9cc8e );
/*TODO*///		ROM_LOAD( "sf2gfx11.rom",       0x200000, 0x80000, 0x2c7e2229 );
/*TODO*///		ROM_LOAD( "sf2gfx21.rom",       0x280000, 0x80000, 0x994bfa58 );
/*TODO*///		ROM_LOAD( "sf2gfx03.rom",       0x300000, 0x80000, 0x4b1b33a8 );
/*TODO*///		ROM_LOAD( "sf2gfx12.rom",       0x380000, 0x80000, 0x5e9cd89a );
/*TODO*///		ROM_LOAD( "sf2gfx22.rom",       0x400000, 0x80000, 0x0627c831 );
/*TODO*///		ROM_LOAD( "sf2gfx04.rom",       0x480000, 0x80000, 0x57213be8 );
/*TODO*///		ROM_LOAD( "sf2gfx13.rom",       0x500000, 0x80000, 0xb5548f17 );
/*TODO*///		ROM_LOAD( "sf2gfx23.rom",       0x580000, 0x80000, 0x3e66ad9d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2_09.rom",    0x00000, 0x08000, 0xa4823a1b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2_18.rom",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2_19.rom",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_3wonders = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "3wonders.30",  0x00000, 0x20000, 0x0b156fd8 );
/*TODO*///		ROM_LOAD_ODD ( "3wonders.35",  0x00000, 0x20000, 0x57350bf4 );
/*TODO*///		ROM_LOAD_EVEN( "3wonders.31",  0x40000, 0x20000, 0x0e723fcc );
/*TODO*///		ROM_LOAD_ODD ( "3wonders.36",  0x40000, 0x20000, 0x523a45dc );
/*TODO*///		ROM_LOAD_EVEN( "3wonders.28",  0x80000, 0x20000, 0x054137c8 );
/*TODO*///		ROM_LOAD_ODD ( "3wonders.33",  0x80000, 0x20000, 0x7264cb1b );
/*TODO*///		ROM_LOAD_EVEN( "3wonders.29",  0xc0000, 0x20000, 0x37ba3e20 );
/*TODO*///		ROM_LOAD_ODD ( "3wonders.34",  0xc0000, 0x20000, 0xf99f46c0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "3wonders.01",  0x000000, 0x80000, 0x902489d0 );
/*TODO*///		ROM_LOAD( "3wonders.02",  0x080000, 0x80000, 0xe9a034f4 );
/*TODO*///		ROM_LOAD( "3wonders.05",  0x100000, 0x80000, 0x86aef804 );
/*TODO*///		ROM_LOAD( "3wonders.06",  0x180000, 0x80000, 0x13cb0e7c );
/*TODO*///		ROM_LOAD( "3wonders.03",  0x200000, 0x80000, 0xe35ce720 );
/*TODO*///		ROM_LOAD( "3wonders.04",  0x280000, 0x80000, 0xdf0eea8b );
/*TODO*///		ROM_LOAD( "3wonders.07",  0x300000, 0x80000, 0x4f057110 );
/*TODO*///		ROM_LOAD( "3wonders.08",  0x380000, 0x80000, 0x1f055014 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "3wonders.09",   0x00000, 0x08000, 0xabfca165 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "3wonders.18",  0x00000, 0x20000, 0x26b211ab );
/*TODO*///		ROM_LOAD( "3wonders.19",  0x20000, 0x20000, 0xdbe64ad0 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_wonder3 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "rtj36.bin",    0x00000, 0x20000, 0xe3741247 );
/*TODO*///		ROM_LOAD_ODD ( "rtj42.bin",    0x00000, 0x20000, 0xb4baa117 );
/*TODO*///		ROM_LOAD_EVEN( "rtj37.bin",    0x40000, 0x20000, 0xa1f677b0 );
/*TODO*///		ROM_LOAD_ODD ( "rtj43.bin",    0x40000, 0x20000, 0x85337a47 );
/*TODO*///	
/*TODO*///		ROM_LOAD_EVEN( "3wonders.28",  0x80000, 0x20000, 0x054137c8 );
/*TODO*///		ROM_LOAD_ODD ( "3wonders.33",  0x80000, 0x20000, 0x7264cb1b );
/*TODO*///		ROM_LOAD_EVEN( "rtj35.bin",    0xc0000, 0x20000, 0xe72f9ea3 );
/*TODO*///		ROM_LOAD_ODD ( "rtj41.bin",    0xc0000, 0x20000, 0xa11ee998 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "3wonders.01",  0x000000, 0x80000, 0x902489d0 );
/*TODO*///		ROM_LOAD( "3wonders.02",  0x080000, 0x80000, 0xe9a034f4 );
/*TODO*///		ROM_LOAD( "3wonders.05",  0x100000, 0x80000, 0x86aef804 );
/*TODO*///		ROM_LOAD( "3wonders.06",  0x180000, 0x80000, 0x13cb0e7c );
/*TODO*///		ROM_LOAD( "3wonders.03",  0x200000, 0x80000, 0xe35ce720 );
/*TODO*///		ROM_LOAD( "3wonders.04",  0x280000, 0x80000, 0xdf0eea8b );
/*TODO*///		ROM_LOAD( "3wonders.07",  0x300000, 0x80000, 0x4f057110 );
/*TODO*///		ROM_LOAD( "3wonders.08",  0x380000, 0x80000, 0x1f055014 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "rt23.bin",      0x00000, 0x08000, 0x7d5a77a7 );   /* could have one bad byte */
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );               /* (compare with US version, */
/*TODO*///															/* which is verified to be correct) */
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "3wonders.18",  0x00000, 0x20000, 0x26b211ab );
/*TODO*///		ROM_LOAD( "3wonders.19",  0x20000, 0x20000, 0xdbe64ad0 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_kod = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "kod30.rom",    0x00000, 0x20000, 0xc7414fd4 );
/*TODO*///		ROM_LOAD_ODD ( "kod37.rom",    0x00000, 0x20000, 0xa5bf40d2 );
/*TODO*///		ROM_LOAD_EVEN( "kod31.rom",    0x40000, 0x20000, 0x1fffc7bd );
/*TODO*///		ROM_LOAD_ODD ( "kod38.rom",    0x40000, 0x20000, 0x89e57a82 );
/*TODO*///		ROM_LOAD_EVEN( "kod28.rom",    0x80000, 0x20000, 0x9367bcd9 );
/*TODO*///		ROM_LOAD_ODD ( "kod35.rom",    0x80000, 0x20000, 0x4ca6a48a );
/*TODO*///		ROM_LOAD_EVEN( "kod29.rom",    0xc0000, 0x20000, 0x6a0ba878 );
/*TODO*///		ROM_LOAD_ODD ( "kod36.rom",    0xc0000, 0x20000, 0xb509b39d );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "kod01.rom",    0x000000, 0x80000, 0x5f74bf78 );
/*TODO*///		ROM_LOAD( "kod10.rom",    0x080000, 0x80000, 0x9ef36604 );
/*TODO*///		ROM_LOAD( "kod02.rom",    0x100000, 0x80000, 0xe45b8701 );
/*TODO*///		ROM_LOAD( "kod11.rom",    0x180000, 0x80000, 0x113358f3 );
/*TODO*///		ROM_LOAD( "kod03.rom",    0x200000, 0x80000, 0x5e5303bf );
/*TODO*///		ROM_LOAD( "kod12.rom",    0x280000, 0x80000, 0x402b9b4f );
/*TODO*///		ROM_LOAD( "kod04.rom",    0x300000, 0x80000, 0xa7750322 );
/*TODO*///		ROM_LOAD( "kod13.rom",    0x380000, 0x80000, 0x38853c44 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kod09.rom",     0x00000, 0x08000, 0xf5514510 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kod18.rom",    0x00000, 0x20000, 0x69ecb2c8 );
/*TODO*///		ROM_LOAD( "kod19.rom",    0x20000, 0x20000, 0x02d851c1 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_kodj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "kd30.bin",    0x00000, 0x20000, 0xebc788ad );
/*TODO*///		ROM_LOAD_ODD ( "kd37.bin",    0x00000, 0x20000, 0xe55c3529 );
/*TODO*///		ROM_LOAD_EVEN( "kd31.bin",    0x40000, 0x20000, 0xc710d722 );
/*TODO*///		ROM_LOAD_ODD ( "kd38.bin",    0x40000, 0x20000, 0x57d6ed3a );
/*TODO*///		ROM_LOAD_WIDE_SWAP("kd33.bin",0x80000,  0x80000, 0x9bd7ad4b);
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "kod01.rom",    0x000000, 0x80000, 0x5f74bf78 );
/*TODO*///		ROM_LOAD( "kod10.rom",    0x080000, 0x80000, 0x9ef36604 );
/*TODO*///		ROM_LOAD( "kod02.rom",    0x100000, 0x80000, 0xe45b8701 );
/*TODO*///		ROM_LOAD( "kod11.rom",    0x180000, 0x80000, 0x113358f3 );
/*TODO*///		ROM_LOAD( "kod03.rom",    0x200000, 0x80000, 0x5e5303bf );
/*TODO*///		ROM_LOAD( "kod12.rom",    0x280000, 0x80000, 0x402b9b4f );
/*TODO*///		ROM_LOAD( "kod04.rom",    0x300000, 0x80000, 0xa7750322 );
/*TODO*///		ROM_LOAD( "kod13.rom",    0x380000, 0x80000, 0x38853c44 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kd09.bin",      0x00000, 0x08000, 0xbac6ec26 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kd18.bin",    0x00000, 0x20000, 0x4c63181d );
/*TODO*///		ROM_LOAD( "kd19.bin",    0x20000, 0x20000, 0x92941b80 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_kodb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "kod.17",    0x00000, 0x080000, 0x036dd74c );
/*TODO*///		ROM_LOAD_ODD ( "kod.18",    0x00000, 0x080000, 0x3e4b7295 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "kod.ci",   0x000000, 0x80000, 0x22228bc5 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "kod.di",   0x000000, 0x80000, 0xab031763 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "kod.cp",   0x100000, 0x80000, 0xe3b8589e )
/*TODO*///		ROM_LOAD_GFX_ODD ( "kod.dp",   0x100000, 0x80000, 0x3eec9580 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "kod.ai",   0x200000, 0x80000, 0xcffbf4be )
/*TODO*///		ROM_LOAD_GFX_ODD ( "kod.bi",   0x200000, 0x80000, 0x4a1b43fe )
/*TODO*///		ROM_LOAD_GFX_EVEN( "kod.ap",   0x300000, 0x80000, 0xfdf5f163 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "kod.bp",   0x300000, 0x80000, 0x4e1c52b7 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kod.15",        0x00000, 0x08000, 0x01cae60c );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kd18.bin",    0x00000, 0x20000, 0x4c63181d );
/*TODO*///		ROM_LOAD( "kd19.bin",    0x20000, 0x20000, 0x92941b80 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_captcomm = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cce_23d.rom",  0x000000, 0x80000, 0x19c58ece );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cc_22d.rom",   0x080000, 0x80000, 0xa91949b7 );
/*TODO*///		ROM_LOAD_EVEN( "cc_24d.rom",        0x100000, 0x20000, 0x680e543f );
/*TODO*///		ROM_LOAD_ODD ( "cc_28d.rom",        0x100000, 0x20000, 0x8820039f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gfx_02.rom",   0x000000, 0x80000, 0x00637302 );
/*TODO*///		ROM_LOAD( "gfx_06.rom",   0x080000, 0x80000, 0x0c69f151 );
/*TODO*///		ROM_LOAD( "gfx_01.rom",   0x100000, 0x80000, 0x7261d8ba );
/*TODO*///		ROM_LOAD( "gfx_05.rom",   0x180000, 0x80000, 0x28718bed );
/*TODO*///		ROM_LOAD( "gfx_04.rom",   0x200000, 0x80000, 0xcc87cf61 );
/*TODO*///		ROM_LOAD( "gfx_08.rom",   0x280000, 0x80000, 0x1f9ebb97 );
/*TODO*///		ROM_LOAD( "gfx_03.rom",   0x300000, 0x80000, 0x6a60f949 );
/*TODO*///		ROM_LOAD( "gfx_07.rom",   0x380000, 0x80000, 0xd4acc53a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "cc_09.rom",     0x00000, 0x08000, 0x698e8b58 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "cc_18.rom",    0x00000, 0x20000, 0x6de2c2db );
/*TODO*///		ROM_LOAD( "cc_19.rom",    0x20000, 0x20000, 0xb99091ae );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_captcomu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "23b",   0x000000, 0x80000, 0x03da44fd );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "22c",   0x080000, 0x80000, 0x9b82a052 );
/*TODO*///		ROM_LOAD_EVEN( "24b",        0x100000, 0x20000, 0x84ff99b2 );
/*TODO*///		ROM_LOAD_ODD ( "28b",        0x100000, 0x20000, 0xfbcec223 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gfx_02.rom",   0x000000, 0x80000, 0x00637302 );
/*TODO*///		ROM_LOAD( "gfx_06.rom",   0x080000, 0x80000, 0x0c69f151 );
/*TODO*///		ROM_LOAD( "gfx_01.rom",   0x100000, 0x80000, 0x7261d8ba );
/*TODO*///		ROM_LOAD( "gfx_05.rom",   0x180000, 0x80000, 0x28718bed );
/*TODO*///		ROM_LOAD( "gfx_04.rom",   0x200000, 0x80000, 0xcc87cf61 );
/*TODO*///		ROM_LOAD( "gfx_08.rom",   0x280000, 0x80000, 0x1f9ebb97 );
/*TODO*///		ROM_LOAD( "gfx_03.rom",   0x300000, 0x80000, 0x6a60f949 );
/*TODO*///		ROM_LOAD( "gfx_07.rom",   0x380000, 0x80000, 0xd4acc53a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "cc_09.rom",     0x00000, 0x08000, 0x698e8b58 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "cc_18.rom",    0x00000, 0x20000, 0x6de2c2db );
/*TODO*///		ROM_LOAD( "cc_19.rom",    0x20000, 0x20000, 0xb99091ae );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_captcomj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cc23.bin",   0x000000, 0x80000, 0x5b482b62 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cc22.bin",   0x080000, 0x80000, 0x0fd34195 );
/*TODO*///		ROM_LOAD_EVEN( "cc24.bin",        0x100000, 0x20000, 0x3a794f25 );
/*TODO*///		ROM_LOAD_ODD ( "cc28.bin",        0x100000, 0x20000, 0xfc3c2906 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "gfx_02.rom",   0x000000, 0x80000, 0x00637302 );
/*TODO*///		ROM_LOAD( "gfx_06.rom",   0x080000, 0x80000, 0x0c69f151 );
/*TODO*///		ROM_LOAD( "gfx_01.rom",   0x100000, 0x80000, 0x7261d8ba );
/*TODO*///		ROM_LOAD( "gfx_05.rom",   0x180000, 0x80000, 0x28718bed );
/*TODO*///		ROM_LOAD( "gfx_04.rom",   0x200000, 0x80000, 0xcc87cf61 );
/*TODO*///		ROM_LOAD( "gfx_08.rom",   0x280000, 0x80000, 0x1f9ebb97 );
/*TODO*///		ROM_LOAD( "gfx_03.rom",   0x300000, 0x80000, 0x6a60f949 );
/*TODO*///		ROM_LOAD( "gfx_07.rom",   0x380000, 0x80000, 0xd4acc53a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "cc_09.rom",     0x00000, 0x08000, 0x698e8b58 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "cc_18.rom",    0x00000, 0x20000, 0x6de2c2db );
/*TODO*///		ROM_LOAD( "cc_19.rom",    0x20000, 0x20000, 0xb99091ae );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_knights = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "kr_23e.rom",   0x00000, 0x80000, 0x1b3997eb );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "kr_22.rom",    0x80000, 0x80000, 0xd0b671a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "kr_gfx2.rom",  0x000000, 0x80000, 0xf095be2d );
/*TODO*///		ROM_LOAD( "kr_gfx6.rom",  0x080000, 0x80000, 0x0200bc3d );
/*TODO*///		ROM_LOAD( "kr_gfx1.rom",  0x100000, 0x80000, 0x9e36c1a4 );
/*TODO*///		ROM_LOAD( "kr_gfx5.rom",  0x180000, 0x80000, 0x1f4298d2 );
/*TODO*///		ROM_LOAD( "kr_gfx4.rom",  0x200000, 0x80000, 0x179dfd96 );
/*TODO*///		ROM_LOAD( "kr_gfx8.rom",  0x280000, 0x80000, 0x0bb2b4e7 );
/*TODO*///		ROM_LOAD( "kr_gfx3.rom",  0x300000, 0x80000, 0xc5832cae );
/*TODO*///		ROM_LOAD( "kr_gfx7.rom",  0x380000, 0x80000, 0x37fa8751 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kr_09.rom",     0x00000, 0x08000, 0x5e44d9ee );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kr_18.rom",    0x00000, 0x20000, 0xda69d15f );
/*TODO*///		ROM_LOAD( "kr_19.rom",    0x20000, 0x20000, 0xbfc654e9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_knightsu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "kru23.rom",    0x00000, 0x80000, 0x252bc2ba );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "kr_22.rom",    0x80000, 0x80000, 0xd0b671a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "kr_gfx2.rom",  0x000000, 0x80000, 0xf095be2d );
/*TODO*///		ROM_LOAD( "kr_gfx6.rom",  0x080000, 0x80000, 0x0200bc3d );
/*TODO*///		ROM_LOAD( "kr_gfx1.rom",  0x100000, 0x80000, 0x9e36c1a4 );
/*TODO*///		ROM_LOAD( "kr_gfx5.rom",  0x180000, 0x80000, 0x1f4298d2 );
/*TODO*///		ROM_LOAD( "kr_gfx4.rom",  0x200000, 0x80000, 0x179dfd96 );
/*TODO*///		ROM_LOAD( "kr_gfx8.rom",  0x280000, 0x80000, 0x0bb2b4e7 );
/*TODO*///		ROM_LOAD( "kr_gfx3.rom",  0x300000, 0x80000, 0xc5832cae );
/*TODO*///		ROM_LOAD( "kr_gfx7.rom",  0x380000, 0x80000, 0x37fa8751 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kr_09.rom",     0x00000, 0x08000, 0x5e44d9ee );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kr_18.rom",    0x00000, 0x20000, 0xda69d15f );
/*TODO*///		ROM_LOAD( "kr_19.rom",    0x20000, 0x20000, 0xbfc654e9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_knightsj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "krj30.bin",   0x00000, 0x20000, 0xad3d1a8e );
/*TODO*///		ROM_LOAD_ODD ( "krj37.bin",   0x00000, 0x20000, 0xe694a491 );
/*TODO*///		ROM_LOAD_EVEN( "krj31.bin",   0x40000, 0x20000, 0x85596094 );
/*TODO*///		ROM_LOAD_ODD ( "krj38.bin",   0x40000, 0x20000, 0x9198bf8f );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "kr_22.rom",    0x80000, 0x80000, 0xd0b671a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "kr_gfx2.rom",  0x000000, 0x80000, 0xf095be2d );
/*TODO*///		ROM_LOAD( "kr_gfx6.rom",  0x080000, 0x80000, 0x0200bc3d );
/*TODO*///		ROM_LOAD( "kr_gfx1.rom",  0x100000, 0x80000, 0x9e36c1a4 );
/*TODO*///		ROM_LOAD( "kr_gfx5.rom",  0x180000, 0x80000, 0x1f4298d2 );
/*TODO*///		ROM_LOAD( "kr_gfx4.rom",  0x200000, 0x80000, 0x179dfd96 );
/*TODO*///		ROM_LOAD( "kr_gfx8.rom",  0x280000, 0x80000, 0x0bb2b4e7 );
/*TODO*///		ROM_LOAD( "kr_gfx3.rom",  0x300000, 0x80000, 0xc5832cae );
/*TODO*///		ROM_LOAD( "kr_gfx7.rom",  0x380000, 0x80000, 0x37fa8751 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "kr_09.rom",     0x00000, 0x08000, 0x5e44d9ee );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "kr_18.rom",    0x00000, 0x20000, 0xda69d15f );
/*TODO*///		ROM_LOAD( "kr_19.rom",    0x20000, 0x20000, 0xbfc654e9 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2ce = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ce.23",     0x000000, 0x80000, 0x3f846b74 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ce.22",     0x080000, 0x80000, 0x99f1cca4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ce.21",     0x100000, 0x80000, 0x925a7877 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2cea = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "s92u-23a",     0x000000, 0x80000, 0xac44415b );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ce.22",     0x080000, 0x80000, 0x99f1cca4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ce.21",     0x100000, 0x80000, 0x925a7877 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2ceb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "s92-23b",      0x000000, 0x80000, 0x996a3015 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "s92-22b",      0x080000, 0x80000, 0x2bbe15ed );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "s92-21b",      0x100000, 0x80000, 0xb383cb1c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2cej = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE( "sf2cej.23",    0x000000, 0x80000, 0x7c463f94 );
/*TODO*///		ROM_LOAD_WIDE( "sf2cej.22",    0x080000, 0x80000, 0x6628f6a6 );
/*TODO*///		ROM_LOAD_WIDE( "sf2cej.21",    0x100000, 0x80000, 0xfcb8fe8f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2rb = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE( "sf2d__23.rom", 0x000000, 0x80000, 0x450532b0 );
/*TODO*///		ROM_LOAD_WIDE( "sf2d__22.rom", 0x080000, 0x80000, 0xfe9d9cf5 );
/*TODO*///		ROM_LOAD_WIDE( "sf2cej.21",    0x100000, 0x80000, 0xfcb8fe8f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2red = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2red.23",    0x000000, 0x80000, 0x40276abb );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2red.22",    0x080000, 0x80000, 0x18daf387 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2red.21",    0x100000, 0x80000, 0x52c486bb );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2accp2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ca-23.bin", 0x000000, 0x80000, 0x36c3ba2f );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ca-22.bin", 0x080000, 0x80000, 0x0550453d );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2ca-21.bin", 0x100000, 0x40000, 0x4c1c43ba );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2.11",       0x100000, 0x80000, 0xd6ec9a0a );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2.10",       0x280000, 0x80000, 0x960687d5 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2.13",       0x400000, 0x80000, 0xed2c67f6 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2.12",       0x580000, 0x80000, 0x978ecd18 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_varth = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "vae_30a.rom",  0x00000, 0x20000, 0x7fcd0091 );
/*TODO*///		ROM_LOAD_ODD ( "vae_35a.rom",  0x00000, 0x20000, 0x35cf9509 );
/*TODO*///		ROM_LOAD_EVEN( "vae_31a.rom",  0x40000, 0x20000, 0x15e5ee81 );
/*TODO*///		ROM_LOAD_ODD ( "vae_36a.rom",  0x40000, 0x20000, 0x153a201e );
/*TODO*///		ROM_LOAD_EVEN( "vae_28a.rom",  0x80000, 0x20000, 0x7a0e0d25 );
/*TODO*///		ROM_LOAD_ODD ( "vae_33a.rom",  0x80000, 0x20000, 0xf2365922 );
/*TODO*///		ROM_LOAD_EVEN( "vae_29a.rom",  0xc0000, 0x20000, 0x5e2cd2c3 );
/*TODO*///		ROM_LOAD_ODD ( "vae_34a.rom",  0xc0000, 0x20000, 0x3d9bdf83 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "va_gfx1.rom",  0x000000, 0x80000, 0x0b1ace37 );
/*TODO*///		ROM_LOAD( "va_gfx5.rom",  0x080000, 0x80000, 0xb1fb726e );
/*TODO*///		ROM_LOAD( "va_gfx3.rom",  0x100000, 0x80000, 0x44dfe706 );
/*TODO*///		ROM_LOAD( "va_gfx7.rom",  0x180000, 0x80000, 0x4c6588cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "va_09.rom",     0x00000, 0x08000, 0x7a99446e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "va_18.rom",    0x00000, 0x20000, 0xde30510e );
/*TODO*///		ROM_LOAD( "va_19.rom",    0x20000, 0x20000, 0x0610a4ac );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_varthu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "vau23a.bin",  0x00000, 0x80000, 0xfbe68726 );
/*TODO*///		ROM_LOAD_EVEN( "vae_28a.rom",  0x80000, 0x20000, 0x7a0e0d25 );
/*TODO*///		ROM_LOAD_ODD ( "vae_33a.rom",  0x80000, 0x20000, 0xf2365922 );
/*TODO*///		ROM_LOAD_EVEN( "vae_29a.rom",  0xc0000, 0x20000, 0x5e2cd2c3 );
/*TODO*///		ROM_LOAD_ODD ( "vae_34a.rom",  0xc0000, 0x20000, 0x3d9bdf83 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "va_gfx1.rom",  0x000000, 0x80000, 0x0b1ace37 );
/*TODO*///		ROM_LOAD( "va_gfx5.rom",  0x080000, 0x80000, 0xb1fb726e );
/*TODO*///		ROM_LOAD( "va_gfx3.rom",  0x100000, 0x80000, 0x44dfe706 );
/*TODO*///		ROM_LOAD( "va_gfx7.rom",  0x180000, 0x80000, 0x4c6588cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "va_09.rom",     0x00000, 0x08000, 0x7a99446e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "va_18.rom",    0x00000, 0x20000, 0xde30510e );
/*TODO*///		ROM_LOAD( "va_19.rom",    0x20000, 0x20000, 0x0610a4ac );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_varthj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "vaj36b.bin",   0x00000, 0x20000, 0x1d798d6a );
/*TODO*///		ROM_LOAD_ODD ( "vaj42b.bin",   0x00000, 0x20000, 0x0f720233 );
/*TODO*///		ROM_LOAD_EVEN( "vaj37b.bin",   0x40000, 0x20000, 0x24414b17 );
/*TODO*///		ROM_LOAD_ODD ( "vaj43b.bin",   0x40000, 0x20000, 0x34b4b06c );
/*TODO*///		ROM_LOAD_EVEN( "vaj34b.bin",   0x80000, 0x20000, 0x87c79aed );
/*TODO*///		ROM_LOAD_ODD ( "vaj40b.bin",   0x80000, 0x20000, 0x210b4bd0 );
/*TODO*///		ROM_LOAD_EVEN( "vaj35b.bin",   0xc0000, 0x20000, 0x6b0da69f );
/*TODO*///		ROM_LOAD_ODD ( "vaj41b.bin",   0xc0000, 0x20000, 0x6542c8a4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "va_gfx1.rom",  0x000000, 0x80000, 0x0b1ace37 );
/*TODO*///		ROM_LOAD( "va_gfx5.rom",  0x080000, 0x80000, 0xb1fb726e );
/*TODO*///		ROM_LOAD( "va_gfx3.rom",  0x100000, 0x80000, 0x44dfe706 );
/*TODO*///		ROM_LOAD( "va_gfx7.rom",  0x180000, 0x80000, 0x4c6588cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "va_09.rom",     0x00000, 0x08000, 0x7a99446e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "va_18.rom",    0x00000, 0x20000, 0xde30510e );
/*TODO*///		ROM_LOAD( "va_19.rom",    0x20000, 0x20000, 0x0610a4ac );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_cworld2j = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "q536.bin",       0x00000, 0x20000, 0x38a08099 );
/*TODO*///		ROM_LOAD_ODD ( "q542.bin",       0x00000, 0x20000, 0x4d29b3a4 );
/*TODO*///		ROM_LOAD_EVEN( "q537.bin",       0x40000, 0x20000, 0xeb547ebc );
/*TODO*///		ROM_LOAD_ODD ( "q543.bin",       0x40000, 0x20000, 0x3ef65ea8 );
/*TODO*///		ROM_LOAD_EVEN( "q534.bin",       0x80000, 0x20000, 0x7fcc1317 );
/*TODO*///		ROM_LOAD_ODD ( "q540.bin",       0x80000, 0x20000, 0x7f14b7b4 );
/*TODO*///		ROM_LOAD_EVEN( "q535.bin",       0xc0000, 0x20000, 0xabacee26 );
/*TODO*///		ROM_LOAD_ODD ( "q541.bin",       0xc0000, 0x20000, 0xd3654067 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "q524.bin",   0x000000, 0x20000, 0xb419d139 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q517.bin",   0x000000, 0x20000, 0xbd3b4d11 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q525.bin",   0x040000, 0x20000, 0x979237cb )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q518.bin",   0x040000, 0x20000, 0xc57da03c )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q509.bin",   0x080000, 0x20000, 0x48496d80 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q501.bin",   0x080000, 0x20000, 0xc5453f56 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q510.bin",   0x0c0000, 0x20000, 0x119e5e93 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q502.bin",   0x0c0000, 0x20000, 0xa2cadcbe )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q538.bin",   0x100000, 0x20000, 0x9c24670c )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q532.bin",   0x100000, 0x20000, 0x3ef9c7c2 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q539.bin",   0x140000, 0x20000, 0xa5839b25 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q533.bin",   0x140000, 0x20000, 0x04d03930 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q513.bin",   0x180000, 0x20000, 0xc741ac52 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q505.bin",   0x180000, 0x20000, 0x143e068f )
/*TODO*///		ROM_LOAD_GFX_EVEN( "q514.bin",   0x1c0000, 0x20000, 0xa8755f82 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "q506.bin",   0x1c0000, 0x20000, 0xc92a91fc )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "q523.bin",      0x00000, 0x08000, 0xe14dc524 );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "q530.bin",       0x00000, 0x20000, 0xd10c1b68 );
/*TODO*///		ROM_LOAD( "q531.bin",       0x20000, 0x20000, 0x7d17e496 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_wof = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2e_23b.rom",  0x000000, 0x80000, 0x11fb2ed1 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2e_22b.rom",  0x080000, 0x80000, 0x479b3f24 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "tk2_gfx2.rom",   0x000000, 0x80000, 0xc5ca2460 );
/*TODO*///		ROM_LOAD( "tk2_gfx6.rom",   0x080000, 0x80000, 0x1abd14d6 );
/*TODO*///		ROM_LOAD( "tk2_gfx1.rom",   0x100000, 0x80000, 0x0d9cb9bf );
/*TODO*///		ROM_LOAD( "tk2_gfx5.rom",   0x180000, 0x80000, 0x291f0f0b );
/*TODO*///		ROM_LOAD( "tk2_gfx4.rom",   0x200000, 0x80000, 0xe349551c );
/*TODO*///		ROM_LOAD( "tk2_gfx8.rom",   0x280000, 0x80000, 0xb27948e3 );
/*TODO*///		ROM_LOAD( "tk2_gfx3.rom",   0x300000, 0x80000, 0x45227027 );
/*TODO*///		ROM_LOAD( "tk2_gfx7.rom",   0x380000, 0x80000, 0x3edeb949 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "tk2_qa.rom",     0x00000, 0x08000, 0xc9183a0d );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "tk2_q1.rom",     0x000000, 0x80000, 0x611268cf );
/*TODO*///		ROM_LOAD( "tk2_q2.rom",     0x080000, 0x80000, 0x20f55ca9 );
/*TODO*///		ROM_LOAD( "tk2_q3.rom",     0x100000, 0x80000, 0xbfcf6f52 );
/*TODO*///		ROM_LOAD( "tk2_q4.rom",     0x180000, 0x80000, 0x36642e88 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_wofa = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2a_23b.rom",  0x000000, 0x80000, 0x2e024628 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2a_22b.rom",  0x080000, 0x80000, 0x900ad4cd );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "tk2_gfx2.rom",   0x000000, 0x80000, 0xc5ca2460 );
/*TODO*///		ROM_LOAD( "tk2_gfx6.rom",   0x080000, 0x80000, 0x1abd14d6 );
/*TODO*///		ROM_LOAD( "tk2_gfx1.rom",   0x100000, 0x80000, 0x0d9cb9bf );
/*TODO*///		ROM_LOAD( "tk2_gfx5.rom",   0x180000, 0x80000, 0x291f0f0b );
/*TODO*///		ROM_LOAD( "tk2_gfx4.rom",   0x200000, 0x80000, 0xe349551c );
/*TODO*///		ROM_LOAD( "tk2_gfx8.rom",   0x280000, 0x80000, 0xb27948e3 );
/*TODO*///		ROM_LOAD( "tk2_gfx3.rom",   0x300000, 0x80000, 0x45227027 );
/*TODO*///		ROM_LOAD( "tk2_gfx7.rom",   0x380000, 0x80000, 0x3edeb949 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "tk2_qa.rom",     0x00000, 0x08000, 0xc9183a0d );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "tk2_q1.rom",     0x000000, 0x80000, 0x611268cf );
/*TODO*///		ROM_LOAD( "tk2_q2.rom",     0x080000, 0x80000, 0x20f55ca9 );
/*TODO*///		ROM_LOAD( "tk2_q3.rom",     0x100000, 0x80000, 0xbfcf6f52 );
/*TODO*///		ROM_LOAD( "tk2_q4.rom",     0x180000, 0x80000, 0x36642e88 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_wofj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2j23c.bin",  0x000000, 0x80000, 0x9b215a68 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "tk2j22c.bin",  0x080000, 0x80000, 0xb74b09ac );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "tk2_gfx2.rom",   0x000000, 0x80000, 0xc5ca2460 );
/*TODO*///		ROM_LOAD( "tk207.bin",      0x080000, 0x80000, 0xd706568e );
/*TODO*///		ROM_LOAD( "tk2_gfx1.rom",   0x100000, 0x80000, 0x0d9cb9bf );
/*TODO*///		ROM_LOAD( "tk205.bin",      0x180000, 0x80000, 0xe4a44d53 );
/*TODO*///		ROM_LOAD( "tk2_gfx4.rom",   0x200000, 0x80000, 0xe349551c );
/*TODO*///		ROM_LOAD( "tk208.bin",      0x280000, 0x80000, 0xd4a19a02 );
/*TODO*///		ROM_LOAD( "tk2_gfx3.rom",   0x300000, 0x80000, 0x45227027 );
/*TODO*///		ROM_LOAD( "tk206.bin",      0x380000, 0x80000, 0x58066ba8 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "tk2_qa.rom",     0x00000, 0x08000, 0xc9183a0d );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "tk2_q1.rom",     0x000000, 0x80000, 0x611268cf );
/*TODO*///		ROM_LOAD( "tk2_q2.rom",     0x080000, 0x80000, 0x20f55ca9 );
/*TODO*///		ROM_LOAD( "tk2_q3.rom",     0x100000, 0x80000, 0xbfcf6f52 );
/*TODO*///		ROM_LOAD( "tk2_q4.rom",     0x180000, 0x80000, 0x36642e88 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2t = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2.23",       0x000000, 0x80000, 0x89a1fc38 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2.22",       0x080000, 0x80000, 0xaea6e035 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2.21",       0x100000, 0x80000, 0xfd200288 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2t.11",      0x100000, 0x80000, 0x293c888c );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2t.10",      0x280000, 0x80000, 0x3c042686 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2t.13",      0x400000, 0x80000, 0x842b35a4 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2t.12",      0x580000, 0x80000, 0x8b7e7183 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sf2tj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2tj.23",   0x000000, 0x80000, 0xea73b4dc );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2.22",     0x080000, 0x80000, 0xaea6e035 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sf2.21",     0x100000, 0x80000, 0xfd200288 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sf2.02",       0x000000, 0x80000, 0xcdb5f027 );
/*TODO*///		ROM_LOAD( "sf2.06",       0x080000, 0x80000, 0x21e3f87d );
/*TODO*///		ROM_LOAD( "sf2t.11",      0x100000, 0x80000, 0x293c888c );
/*TODO*///		ROM_LOAD( "sf2.01",       0x180000, 0x80000, 0x03b0d852 );
/*TODO*///		ROM_LOAD( "sf2.05",       0x200000, 0x80000, 0xba8a2761 );
/*TODO*///		ROM_LOAD( "sf2t.10",      0x280000, 0x80000, 0x3c042686 );
/*TODO*///		ROM_LOAD( "sf2.04",       0x300000, 0x80000, 0xe2799472 );
/*TODO*///		ROM_LOAD( "sf2.08",       0x380000, 0x80000, 0xbefc47df );
/*TODO*///		ROM_LOAD( "sf2t.13",      0x400000, 0x80000, 0x842b35a4 );
/*TODO*///		ROM_LOAD( "sf2.03",       0x480000, 0x80000, 0x840289ec );
/*TODO*///		ROM_LOAD( "sf2.07",       0x500000, 0x80000, 0xe584bfb5 );
/*TODO*///		ROM_LOAD( "sf2t.12",      0x580000, 0x80000, 0x8b7e7183 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sf2.09",        0x00000, 0x08000, 0x08f6b60e );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sf2.18",       0x00000, 0x20000, 0x7f162009 );
/*TODO*///		ROM_LOAD( "sf2.19",       0x20000, 0x20000, 0xbeade53f );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_dino = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cde_23a.rom",  0x000000, 0x80000, 0x8f4e585e );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cde_22a.rom",  0x080000, 0x80000, 0x9278aa12 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cde_21a.rom",  0x100000, 0x80000, 0x66d23de2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "cd_gfx02.rom",   0x000000, 0x80000, 0x09c8fc2d );
/*TODO*///		ROM_LOAD( "cd_gfx06.rom",   0x080000, 0x80000, 0xe7599ac4 );
/*TODO*///		ROM_LOAD( "cd_gfx01.rom",   0x100000, 0x80000, 0x8da4f917 );
/*TODO*///		ROM_LOAD( "cd_gfx05.rom",   0x180000, 0x80000, 0x470befee );
/*TODO*///		ROM_LOAD( "cd_gfx04.rom",   0x200000, 0x80000, 0x637ff38f );
/*TODO*///		ROM_LOAD( "cd_gfx08.rom",   0x280000, 0x80000, 0x211b4b15 );
/*TODO*///		ROM_LOAD( "cd_gfx03.rom",   0x300000, 0x80000, 0x6c40f603 );
/*TODO*///		ROM_LOAD( "cd_gfx07.rom",   0x380000, 0x80000, 0x22bfb7a3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "cd_q.rom",       0x00000, 0x08000, 0x605fdb0b );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "cd_q1.rom",      0x000000, 0x80000, 0x60927775 );
/*TODO*///		ROM_LOAD( "cd_q2.rom",      0x080000, 0x80000, 0x770f4c47 );
/*TODO*///		ROM_LOAD( "cd_q3.rom",      0x100000, 0x80000, 0x2f273ffc );
/*TODO*///		ROM_LOAD( "cd_q4.rom",      0x180000, 0x80000, 0x2c67821d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_dinoj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cdj-23a.8f",   0x000000, 0x80000, 0x5f3ece96 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cdj-22a.7f",   0x080000, 0x80000, 0xa0d8de29 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "cde_21a.rom",  0x100000, 0x80000, 0x66d23de2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "cd_gfx02.rom",   0x000000, 0x80000, 0x09c8fc2d );
/*TODO*///		ROM_LOAD( "cd_gfx06.rom",   0x080000, 0x80000, 0xe7599ac4 );
/*TODO*///		ROM_LOAD( "cd_gfx01.rom",   0x100000, 0x80000, 0x8da4f917 );
/*TODO*///		ROM_LOAD( "cd_gfx05.rom",   0x180000, 0x80000, 0x470befee );
/*TODO*///		ROM_LOAD( "cd_gfx04.rom",   0x200000, 0x80000, 0x637ff38f );
/*TODO*///		ROM_LOAD( "cd_gfx08.rom",   0x280000, 0x80000, 0x211b4b15 );
/*TODO*///		ROM_LOAD( "cd_gfx03.rom",   0x300000, 0x80000, 0x6c40f603 );
/*TODO*///		ROM_LOAD( "cd_gfx07.rom",   0x380000, 0x80000, 0x22bfb7a3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "cd_q.rom",       0x00000, 0x08000, 0x605fdb0b );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "cd_q1.rom",      0x000000, 0x80000, 0x60927775 );
/*TODO*///		ROM_LOAD( "cd_q2.rom",      0x080000, 0x80000, 0x770f4c47 );
/*TODO*///		ROM_LOAD( "cd_q3.rom",      0x100000, 0x80000, 0x2f273ffc );
/*TODO*///		ROM_LOAD( "cd_q4.rom",      0x180000, 0x80000, 0x2c67821d );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_punisher = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "pse_26.rom",       0x000000, 0x20000, 0x389a99d2 );
/*TODO*///		ROM_LOAD_ODD ( "pse_30.rom",       0x000000, 0x20000, 0x68fb06ac );
/*TODO*///		ROM_LOAD_EVEN( "pse_27.rom",       0x040000, 0x20000, 0x3eb181c3 );
/*TODO*///		ROM_LOAD_ODD ( "pse_31.rom",       0x040000, 0x20000, 0x37108e7b );
/*TODO*///		ROM_LOAD_EVEN( "pse_24.rom",       0x080000, 0x20000, 0x0f434414 );
/*TODO*///		ROM_LOAD_ODD ( "pse_28.rom",       0x080000, 0x20000, 0xb732345d );
/*TODO*///		ROM_LOAD_EVEN( "pse_25.rom",       0x0c0000, 0x20000, 0xb77102e2 );
/*TODO*///		ROM_LOAD_ODD ( "pse_29.rom",       0x0c0000, 0x20000, 0xec037bce );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ps_21.rom",   0x100000, 0x80000, 0x8affa5a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ps_gfx2.rom",   0x000000, 0x80000, 0x64fa58d4 );
/*TODO*///		ROM_LOAD( "ps_gfx6.rom",   0x080000, 0x80000, 0xa544f4cc );
/*TODO*///		ROM_LOAD( "ps_gfx1.rom",   0x100000, 0x80000, 0x77b7ccab );
/*TODO*///		ROM_LOAD( "ps_gfx5.rom",   0x180000, 0x80000, 0xc54ea839 );
/*TODO*///		ROM_LOAD( "ps_gfx4.rom",   0x200000, 0x80000, 0x60da42c8 );
/*TODO*///		ROM_LOAD( "ps_gfx8.rom",   0x280000, 0x80000, 0x8f02f436 );
/*TODO*///		ROM_LOAD( "ps_gfx3.rom",   0x300000, 0x80000, 0x0122720b );
/*TODO*///		ROM_LOAD( "ps_gfx7.rom",   0x380000, 0x80000, 0x04c5acbd );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "ps_q.rom",       0x00000, 0x08000, 0x49ff4446 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "ps_q1.rom",      0x000000, 0x80000, 0x31fd8726 );
/*TODO*///		ROM_LOAD( "ps_q2.rom",      0x080000, 0x80000, 0x980a9eef );
/*TODO*///		ROM_LOAD( "ps_q3.rom",      0x100000, 0x80000, 0x0dd44491 );
/*TODO*///		ROM_LOAD( "ps_q4.rom",      0x180000, 0x80000, 0xbed42f03 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_punishru = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN ( "psu26.rom",       0x000000, 0x20000, 0x9236d121 );
/*TODO*///		ROM_LOAD_ODD  ( "psu30.rom",       0x000000, 0x20000, 0x8320e501 );
/*TODO*///		ROM_LOAD_EVEN ( "psu27.rom",       0x040000, 0x20000, 0x61c960a1 );
/*TODO*///		ROM_LOAD_ODD  ( "psu31.rom",       0x040000, 0x20000, 0x78d4c298 );
/*TODO*///		ROM_LOAD_EVEN ( "psu24.rom",       0x080000, 0x20000, 0x1cfecad7 );
/*TODO*///		ROM_LOAD_ODD  ( "psu28.rom",       0x080000, 0x20000, 0xbdf921c1 );
/*TODO*///		ROM_LOAD_EVEN ( "psu25.rom",       0x0c0000, 0x20000, 0xc51acc94 );
/*TODO*///		ROM_LOAD_ODD  ( "psu29.rom",       0x0c0000, 0x20000, 0x52dce1ca );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ps_21.rom",   0x100000, 0x80000, 0x8affa5a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ps_gfx2.rom",   0x000000, 0x80000, 0x64fa58d4 );
/*TODO*///		ROM_LOAD( "ps_gfx6.rom",   0x080000, 0x80000, 0xa544f4cc );
/*TODO*///		ROM_LOAD( "ps_gfx1.rom",   0x100000, 0x80000, 0x77b7ccab );
/*TODO*///		ROM_LOAD( "ps_gfx5.rom",   0x180000, 0x80000, 0xc54ea839 );
/*TODO*///		ROM_LOAD( "ps_gfx4.rom",   0x200000, 0x80000, 0x60da42c8 );
/*TODO*///		ROM_LOAD( "ps_gfx8.rom",   0x280000, 0x80000, 0x8f02f436 );
/*TODO*///		ROM_LOAD( "ps_gfx3.rom",   0x300000, 0x80000, 0x0122720b );
/*TODO*///		ROM_LOAD( "ps_gfx7.rom",   0x380000, 0x80000, 0x04c5acbd );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "ps_q.rom",       0x00000, 0x08000, 0x49ff4446 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "ps_q1.rom",      0x000000, 0x80000, 0x31fd8726 );
/*TODO*///		ROM_LOAD( "ps_q2.rom",      0x080000, 0x80000, 0x980a9eef );
/*TODO*///		ROM_LOAD( "ps_q3.rom",      0x100000, 0x80000, 0x0dd44491 );
/*TODO*///		ROM_LOAD( "ps_q4.rom",      0x180000, 0x80000, 0xbed42f03 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_punishrj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "psj23.bin",   0x000000, 0x80000, 0x6b2fda52 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "psj22.bin",   0x080000, 0x80000, 0xe01036bc );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "ps_21.rom",   0x100000, 0x80000, 0x8affa5a9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "ps_gfx2.rom",   0x000000, 0x80000, 0x64fa58d4 );
/*TODO*///		ROM_LOAD( "ps_gfx6.rom",   0x080000, 0x80000, 0xa544f4cc );
/*TODO*///		ROM_LOAD( "ps_gfx1.rom",   0x100000, 0x80000, 0x77b7ccab );
/*TODO*///		ROM_LOAD( "ps_gfx5.rom",   0x180000, 0x80000, 0xc54ea839 );
/*TODO*///		ROM_LOAD( "ps_gfx4.rom",   0x200000, 0x80000, 0x60da42c8 );
/*TODO*///		ROM_LOAD( "ps_gfx8.rom",   0x280000, 0x80000, 0x8f02f436 );
/*TODO*///		ROM_LOAD( "ps_gfx3.rom",   0x300000, 0x80000, 0x0122720b );
/*TODO*///		ROM_LOAD( "ps_gfx7.rom",   0x380000, 0x80000, 0x04c5acbd );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "ps_q.rom",       0x00000, 0x08000, 0x49ff4446 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "ps_q1.rom",      0x000000, 0x80000, 0x31fd8726 );
/*TODO*///		ROM_LOAD( "ps_q2.rom",      0x080000, 0x80000, 0x980a9eef );
/*TODO*///		ROM_LOAD( "ps_q3.rom",      0x100000, 0x80000, 0x0dd44491 );
/*TODO*///		ROM_LOAD( "ps_q4.rom",      0x180000, 0x80000, 0xbed42f03 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_slammast = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_23e.rom",  0x000000, 0x80000, 0x5394057a );
/*TODO*///		ROM_LOAD_EVEN( "mbe_24b.rom",       0x080000, 0x20000, 0x95d5e729 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_28b.rom",       0x080000, 0x20000, 0xb1c7cbcb );
/*TODO*///		ROM_LOAD_EVEN( "mbe_25b.rom",       0x0c0000, 0x20000, 0xa50d3fd4 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_29b.rom",       0x0c0000, 0x20000, 0x08e32e56 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_21a.rom",  0x100000, 0x80000, 0xd5007b05 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_20a.rom",  0x180000, 0x80000, 0xaeb557b0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "mb_gfx02.rom",   0x000000, 0x80000, 0x2ffbfea8 );
/*TODO*///		ROM_LOAD( "mb_gfx06.rom",   0x080000, 0x80000, 0xb76c70e9 );
/*TODO*///		ROM_LOAD( "mb_gfx11.rom",   0x100000, 0x80000, 0x8fb94743 );
/*TODO*///		ROM_LOAD( "mb_gfx01.rom",   0x180000, 0x80000, 0x41468e06 );
/*TODO*///		ROM_LOAD( "mb_gfx05.rom",   0x200000, 0x80000, 0x506b9dc9 );
/*TODO*///		ROM_LOAD( "mb_gfx10.rom",   0x280000, 0x80000, 0x97976ff5 );
/*TODO*///		ROM_LOAD( "mb_gfx04.rom",   0x300000, 0x80000, 0x1eb9841d );
/*TODO*///		ROM_LOAD( "mb_gfx08.rom",   0x380000, 0x80000, 0xe60c9556 );
/*TODO*///		ROM_LOAD( "mb_gfx13.rom",   0x400000, 0x80000, 0xda810d5f );
/*TODO*///		ROM_LOAD( "mb_gfx03.rom",   0x480000, 0x80000, 0xf453aa9e );
/*TODO*///		ROM_LOAD( "mb_gfx07.rom",   0x500000, 0x80000, 0xaff8c2fb );
/*TODO*///		ROM_LOAD( "mb_gfx12.rom",   0x580000, 0x80000, 0xb350a840 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "mb_qa.rom",      0x00000, 0x08000, 0xe21a03c4 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x8000, REGION_USER1 );
/*TODO*///		/* the encrypted Z80 ROM will be copied here, where the main CPU can read it. */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "mb_q1.rom",      0x000000, 0x80000, 0x0630c3ce );
/*TODO*///		ROM_LOAD( "mb_q2.rom",      0x080000, 0x80000, 0x354f9c21 );
/*TODO*///		ROM_LOAD( "mb_q3.rom",      0x100000, 0x80000, 0x7838487c );
/*TODO*///		ROM_LOAD( "mb_q4.rom",      0x180000, 0x80000, 0xab66e087 );
/*TODO*///		ROM_LOAD( "mb_q5.rom",      0x200000, 0x80000, 0xc789fef2 );
/*TODO*///		ROM_LOAD( "mb_q6.rom",      0x280000, 0x80000, 0xecb81b61 );
/*TODO*///		ROM_LOAD( "mb_q7.rom",      0x300000, 0x80000, 0x041e49ba );
/*TODO*///		ROM_LOAD( "mb_q8.rom",      0x380000, 0x80000, 0x59fe702a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_slammasu = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbu-23e.rom",  0x000000, 0x80000, 0x224f0062 );
/*TODO*///		ROM_LOAD_EVEN( "mbe_24b.rom",       0x080000, 0x20000, 0x95d5e729 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_28b.rom",       0x080000, 0x20000, 0xb1c7cbcb );
/*TODO*///		ROM_LOAD_EVEN( "mbe_25b.rom",       0x0c0000, 0x20000, 0xa50d3fd4 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_29b.rom",       0x0c0000, 0x20000, 0x08e32e56 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_21a.rom",  0x100000, 0x80000, 0xd5007b05 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbu-20a.rom",  0x180000, 0x80000, 0xfc848af5 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "mb_gfx02.rom",   0x000000, 0x80000, 0x2ffbfea8 );
/*TODO*///		ROM_LOAD( "mb_gfx06.rom",   0x080000, 0x80000, 0xb76c70e9 );
/*TODO*///		ROM_LOAD( "mb_gfx11.rom",   0x100000, 0x80000, 0x8fb94743 );
/*TODO*///		ROM_LOAD( "mb_gfx01.rom",   0x180000, 0x80000, 0x41468e06 );
/*TODO*///		ROM_LOAD( "mb_gfx05.rom",   0x200000, 0x80000, 0x506b9dc9 );
/*TODO*///		ROM_LOAD( "mb_gfx10.rom",   0x280000, 0x80000, 0x97976ff5 );
/*TODO*///		ROM_LOAD( "mb_gfx04.rom",   0x300000, 0x80000, 0x1eb9841d );
/*TODO*///		ROM_LOAD( "mb_gfx08.rom",   0x380000, 0x80000, 0xe60c9556 );
/*TODO*///		ROM_LOAD( "mb_gfx13.rom",   0x400000, 0x80000, 0xda810d5f );
/*TODO*///		ROM_LOAD( "mb_gfx03.rom",   0x480000, 0x80000, 0xf453aa9e );
/*TODO*///		ROM_LOAD( "mb_gfx07.rom",   0x500000, 0x80000, 0xaff8c2fb );
/*TODO*///		ROM_LOAD( "mb_gfx12.rom",   0x580000, 0x80000, 0xb350a840 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "mb_qa.rom",      0x00000, 0x08000, 0xe21a03c4 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x8000, REGION_USER1 );
/*TODO*///		/* the encrypted Z80 ROM will be copied here, where the main CPU can read it. */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "mb_q1.rom",      0x000000, 0x80000, 0x0630c3ce );
/*TODO*///		ROM_LOAD( "mb_q2.rom",      0x080000, 0x80000, 0x354f9c21 );
/*TODO*///		ROM_LOAD( "mb_q3.rom",      0x100000, 0x80000, 0x7838487c );
/*TODO*///		ROM_LOAD( "mb_q4.rom",      0x180000, 0x80000, 0xab66e087 );
/*TODO*///		ROM_LOAD( "mb_q5.rom",      0x200000, 0x80000, 0xc789fef2 );
/*TODO*///		ROM_LOAD( "mb_q6.rom",      0x280000, 0x80000, 0xecb81b61 );
/*TODO*///		ROM_LOAD( "mb_q7.rom",      0x300000, 0x80000, 0x041e49ba );
/*TODO*///		ROM_LOAD( "mb_q8.rom",      0x380000, 0x80000, 0x59fe702a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mbomberj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbj23e",       0x000000, 0x80000, 0x0d06036a );
/*TODO*///		ROM_LOAD_EVEN( "mbe_24b.rom",       0x080000, 0x20000, 0x95d5e729 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_28b.rom",       0x080000, 0x20000, 0xb1c7cbcb );
/*TODO*///		ROM_LOAD_EVEN( "mbe_25b.rom",       0x0c0000, 0x20000, 0xa50d3fd4 );
/*TODO*///		ROM_LOAD_ODD ( "mbe_29b.rom",       0x0c0000, 0x20000, 0x08e32e56 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_21a.rom",  0x100000, 0x80000, 0xd5007b05 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbe_20a.rom",  0x180000, 0x80000, 0xaeb557b0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "mb03",           0x000000, 0x80000, 0xcb866c2f );
/*TODO*///		ROM_LOAD( "mb_gfx06.rom",   0x080000, 0x80000, 0xb76c70e9 );
/*TODO*///		ROM_LOAD( "mb_gfx11.rom",   0x100000, 0x80000, 0x8fb94743 );
/*TODO*///		ROM_LOAD( "mb01",           0x180000, 0x80000, 0xa53b1c81 );
/*TODO*///		ROM_LOAD( "mb_gfx05.rom",   0x200000, 0x80000, 0x506b9dc9 );
/*TODO*///		ROM_LOAD( "mb_gfx10.rom",   0x280000, 0x80000, 0x97976ff5 );
/*TODO*///		ROM_LOAD( "mb04",           0x300000, 0x80000, 0xc9143e75 );
/*TODO*///		ROM_LOAD( "mb_gfx08.rom",   0x380000, 0x80000, 0xe60c9556 );
/*TODO*///		ROM_LOAD( "mb_gfx13.rom",   0x400000, 0x80000, 0xda810d5f );
/*TODO*///		ROM_LOAD( "mb02",           0x480000, 0x80000, 0x23fe10f6 );
/*TODO*///		ROM_LOAD( "mb_gfx07.rom",   0x500000, 0x80000, 0xaff8c2fb );
/*TODO*///		ROM_LOAD( "mb_gfx12.rom",   0x580000, 0x80000, 0xb350a840 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "mb_qa.rom",      0x00000, 0x08000, 0xe21a03c4 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x8000, REGION_USER1 );
/*TODO*///		/* the encrypted Z80 ROM will be copied here, where the main CPU can read it. */
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "mb_q1.rom",      0x000000, 0x80000, 0x0630c3ce );
/*TODO*///		ROM_LOAD( "mb_q2.rom",      0x080000, 0x80000, 0x354f9c21 );
/*TODO*///		ROM_LOAD( "mb_q3.rom",      0x100000, 0x80000, 0x7838487c );
/*TODO*///		ROM_LOAD( "mb_q4.rom",      0x180000, 0x80000, 0xab66e087 );
/*TODO*///		ROM_LOAD( "mb_q5.rom",      0x200000, 0x80000, 0xc789fef2 );
/*TODO*///		ROM_LOAD( "mb_q6.rom",      0x280000, 0x80000, 0xecb81b61 );
/*TODO*///		ROM_LOAD( "mb_q7.rom",      0x300000, 0x80000, 0x041e49ba );
/*TODO*///		ROM_LOAD( "mb_q8.rom",      0x380000, 0x80000, 0x59fe702a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mbombrd = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "mbde_26.rom",       0x000000, 0x20000, 0x72b7451c );
/*TODO*///		ROM_LOAD_ODD ( "mbde_30.rom",       0x000000, 0x20000, 0xa036dc16 );
/*TODO*///		ROM_LOAD_EVEN( "mbde_27.rom",       0x040000, 0x20000, 0x4086f534 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_31.rom",       0x040000, 0x20000, 0x085f47f0 );
/*TODO*///		ROM_LOAD_EVEN( "mbde_24.rom",       0x080000, 0x20000, 0xc20895a5 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_28.rom",       0x080000, 0x20000, 0x2618d5e1 );
/*TODO*///		ROM_LOAD_EVEN( "mbde_25.rom",       0x0c0000, 0x20000, 0x9bdb6b11 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_29.rom",       0x0c0000, 0x20000, 0x3f52d5e5 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbde_21.rom",  0x100000, 0x80000, 0x690c026a );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbde_20.rom",  0x180000, 0x80000, 0xb8b2139b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "mb_gfx02.rom",   0x000000, 0x80000, 0x2ffbfea8 );
/*TODO*///		ROM_LOAD( "mb_gfx06.rom",   0x080000, 0x80000, 0xb76c70e9 );
/*TODO*///		ROM_LOAD( "mb_gfx11.rom",   0x100000, 0x80000, 0x8fb94743 );
/*TODO*///		ROM_LOAD( "mb_gfx01.rom",   0x180000, 0x80000, 0x41468e06 );
/*TODO*///		ROM_LOAD( "mb_gfx05.rom",   0x200000, 0x80000, 0x506b9dc9 );
/*TODO*///		ROM_LOAD( "mb_gfx10.rom",   0x280000, 0x80000, 0x97976ff5 );
/*TODO*///		ROM_LOAD( "mb_gfx04.rom",   0x300000, 0x80000, 0x1eb9841d );
/*TODO*///		ROM_LOAD( "mb_gfx08.rom",   0x380000, 0x80000, 0xe60c9556 );
/*TODO*///		ROM_LOAD( "mb_gfx13.rom",   0x400000, 0x80000, 0xda810d5f );
/*TODO*///		ROM_LOAD( "mb_gfx03.rom",   0x480000, 0x80000, 0xf453aa9e );
/*TODO*///		ROM_LOAD( "mb_gfx07.rom",   0x500000, 0x80000, 0xaff8c2fb );
/*TODO*///		ROM_LOAD( "mb_gfx12.rom",   0x580000, 0x80000, 0xb350a840 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "mb_q.rom",       0x00000, 0x08000, 0xd6fa76d1 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "mb_q1.rom",      0x000000, 0x80000, 0x0630c3ce );
/*TODO*///		ROM_LOAD( "mb_q2.rom",      0x080000, 0x80000, 0x354f9c21 );
/*TODO*///		ROM_LOAD( "mb_q3.rom",      0x100000, 0x80000, 0x7838487c );
/*TODO*///		ROM_LOAD( "mb_q4.rom",      0x180000, 0x80000, 0xab66e087 );
/*TODO*///		ROM_LOAD( "mb_q5.rom",      0x200000, 0x80000, 0xc789fef2 );
/*TODO*///		ROM_LOAD( "mb_q6.rom",      0x280000, 0x80000, 0xecb81b61 );
/*TODO*///		ROM_LOAD( "mb_q7.rom",      0x300000, 0x80000, 0x041e49ba );
/*TODO*///		ROM_LOAD( "mb_q8.rom",      0x380000, 0x80000, 0x59fe702a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_mbombrdj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "mbde_26.rom",       0x000000, 0x20000, 0x72b7451c );
/*TODO*///		ROM_LOAD_ODD ( "mbde30.rom",        0x000000, 0x20000, 0xbeff31cf );
/*TODO*///		ROM_LOAD_EVEN( "mbde_27.rom",       0x040000, 0x20000, 0x4086f534 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_31.rom",       0x040000, 0x20000, 0x085f47f0 );
/*TODO*///		ROM_LOAD_EVEN( "mbde_24.rom",       0x080000, 0x20000, 0xc20895a5 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_28.rom",       0x080000, 0x20000, 0x2618d5e1 );
/*TODO*///		ROM_LOAD_EVEN( "mbde_25.rom",       0x0c0000, 0x20000, 0x9bdb6b11 );
/*TODO*///		ROM_LOAD_ODD ( "mbde_29.rom",       0x0c0000, 0x20000, 0x3f52d5e5 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbde_21.rom",  0x100000, 0x80000, 0x690c026a );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "mbde_20.rom",  0x180000, 0x80000, 0xb8b2139b );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x600000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "mb03",           0x000000, 0x80000, 0xcb866c2f );
/*TODO*///		ROM_LOAD( "mb_gfx06.rom",   0x080000, 0x80000, 0xb76c70e9 );
/*TODO*///		ROM_LOAD( "mb_gfx11.rom",   0x100000, 0x80000, 0x8fb94743 );
/*TODO*///		ROM_LOAD( "mb01",           0x180000, 0x80000, 0xa53b1c81 );
/*TODO*///		ROM_LOAD( "mb_gfx05.rom",   0x200000, 0x80000, 0x506b9dc9 );
/*TODO*///		ROM_LOAD( "mb_gfx10.rom",   0x280000, 0x80000, 0x97976ff5 );
/*TODO*///		ROM_LOAD( "mb04",           0x300000, 0x80000, 0xc9143e75 );
/*TODO*///		ROM_LOAD( "mb_gfx08.rom",   0x380000, 0x80000, 0xe60c9556 );
/*TODO*///		ROM_LOAD( "mb_gfx13.rom",   0x400000, 0x80000, 0xda810d5f );
/*TODO*///		ROM_LOAD( "mb02",           0x480000, 0x80000, 0x23fe10f6 );
/*TODO*///		ROM_LOAD( "mb_gfx07.rom",   0x500000, 0x80000, 0xaff8c2fb );
/*TODO*///		ROM_LOAD( "mb_gfx12.rom",   0x580000, 0x80000, 0xb350a840 );
/*TODO*///	
/*TODO*///		ROM_REGION( 2*0x28000, REGION_CPU2 );/* QSound Z80 code + space for decrypted opcodes */
/*TODO*///		ROM_LOAD( "mb_q.rom",       0x00000, 0x08000, 0xd6fa76d1 );
/*TODO*///		ROM_CONTINUE(               0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_SOUND1 );/* QSound samples */
/*TODO*///		ROM_LOAD( "mb_q1.rom",      0x000000, 0x80000, 0x0630c3ce );
/*TODO*///		ROM_LOAD( "mb_q2.rom",      0x080000, 0x80000, 0x354f9c21 );
/*TODO*///		ROM_LOAD( "mb_q3.rom",      0x100000, 0x80000, 0x7838487c );
/*TODO*///		ROM_LOAD( "mb_q4.rom",      0x180000, 0x80000, 0xab66e087 );
/*TODO*///		ROM_LOAD( "mb_q5.rom",      0x200000, 0x80000, 0xc789fef2 );
/*TODO*///		ROM_LOAD( "mb_q6.rom",      0x280000, 0x80000, 0xecb81b61 );
/*TODO*///		ROM_LOAD( "mb_q7.rom",      0x300000, 0x80000, 0x041e49ba );
/*TODO*///		ROM_LOAD( "mb_q8.rom",      0x380000, 0x80000, 0x59fe702a );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_pnickj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "pnij36.bin",   0x00000, 0x20000, 0x2d4ffb2b );
/*TODO*///		ROM_LOAD_ODD ( "pnij42.bin",   0x00000, 0x20000, 0xc085dfaf );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij26.bin",   0x000000, 0x20000, 0xe2af981e )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij18.bin",   0x000000, 0x20000, 0xf17a0e56 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij27.bin",   0x040000, 0x20000, 0x83d5cb0e )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij19.bin",   0x040000, 0x20000, 0xaf08b230 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij09.bin",   0x080000, 0x20000, 0x48177b0a )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij01.bin",   0x080000, 0x20000, 0x01a0f311 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij10.bin",   0x0c0000, 0x20000, 0xc2acc171 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij02.bin",   0x0c0000, 0x20000, 0x0e21fc33 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij38.bin",   0x100000, 0x20000, 0xeb75bd8c )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij32.bin",   0x100000, 0x20000, 0x84560bef )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij39.bin",   0x140000, 0x20000, 0x70fbe579 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij33.bin",   0x140000, 0x20000, 0x3ed2c680 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij13.bin",   0x180000, 0x20000, 0x406451b0 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij05.bin",   0x180000, 0x20000, 0x8c515dc0 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "pnij14.bin",   0x1c0000, 0x20000, 0x7fe59b19 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "pnij06.bin",   0x1c0000, 0x20000, 0x79f4bfe3 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "pnij17.bin",    0x00000, 0x08000, 0xe86f787a );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "pnij24.bin",   0x00000, 0x20000, 0x5092257d );
/*TODO*///		ROM_LOAD( "pnij25.bin",   0x20000, 0x20000, 0x22109aaa );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_qad = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "qdu_36a.rom",  0x00000, 0x20000, 0xde9c24a0 );
/*TODO*///		ROM_LOAD_ODD ( "qdu_42a.rom",  0x00000, 0x20000, 0xcfe36f0c );
/*TODO*///		ROM_LOAD_EVEN( "qdu_37a.rom",  0x40000, 0x20000, 0x10d22320 );
/*TODO*///		ROM_LOAD_ODD ( "qdu_43a.rom",  0x40000, 0x20000, 0x15e6beb9 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD_GFX_EVEN( "qdu_24.rom", 0x000000, 0x20000, 0x2f1bd0ec )
/*TODO*///		ROM_LOAD_GFX_ODD ( "qdu_17.rom", 0x000000, 0x20000, 0xa812f9e2 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "qdu_09.rom", 0x080000, 0x20000, 0x8c3f9f44 )
/*TODO*///		ROM_LOAD_GFX_ODD ( "qdu_01.rom", 0x080000, 0x20000, 0xf688cf8f )
/*TODO*///		ROM_LOAD_GFX_EVEN( "qdu_38.rom", 0x100000, 0x20000, 0xccdddd1f )
/*TODO*///		ROM_LOAD_GFX_ODD ( "qdu_32.rom", 0x100000, 0x20000, 0xa8d295d3 )
/*TODO*///		ROM_LOAD_GFX_EVEN( "qdu_13.rom", 0x180000, 0x20000, 0xafbd551b )
/*TODO*///		ROM_LOAD_GFX_ODD ( "qdu_05.rom", 0x180000, 0x20000, 0xc3db0910 )
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "qdu_23.rom",    0x00000, 0x08000, 0xcfb5264b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "qdu_30.rom",  0x00000, 0x20000, 0xf190da84 );
/*TODO*///		ROM_LOAD( "qdu_31.rom",  0x20000, 0x20000, 0xb7583f73 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_qadj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "qad23a.bin",   0x00000, 0x080000, 0x4d3553de );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "qad22a.bin",   0x80000, 0x80000, 0x3191ddd0 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x200000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "qad03.bin",   0x000000, 0x80000, 0xcea4ca8c );
/*TODO*///		ROM_LOAD( "qad01.bin",   0x080000, 0x80000, 0x9d853b57 );
/*TODO*///		ROM_LOAD( "qad04.bin",   0x100000, 0x80000, 0x41b74d1b );
/*TODO*///		ROM_LOAD( "qad02.bin",   0x180000, 0x80000, 0xb35976c4 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "qad09.bin",     0x00000, 0x08000, 0x733161cc );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "qad18.bin",   0x00000, 0x20000, 0x2bfe6f6a );
/*TODO*///		ROM_LOAD( "qad19.bin",   0x20000, 0x20000, 0x13d3236b );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_qtono2 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_EVEN( "tn2j-30.11e",  0x00000, 0x20000, 0x9226eb5e );
/*TODO*///		ROM_LOAD_ODD ( "tn2j-37.11f",  0x00000, 0x20000, 0xd1d30da1 );
/*TODO*///		ROM_LOAD_EVEN( "tn2j-31.12e",  0x40000, 0x20000, 0x015e6a8a );
/*TODO*///		ROM_LOAD_ODD ( "tn2j-38.12f",  0x40000, 0x20000, 0x1f139bcc );
/*TODO*///		ROM_LOAD_EVEN( "tn2j-28.9e",   0x80000, 0x20000, 0x86d27f71 );
/*TODO*///		ROM_LOAD_ODD ( "tn2j-35.9f",   0x80000, 0x20000, 0x7a1ab87d );
/*TODO*///		ROM_LOAD_EVEN( "tn2j-29.10e",  0xc0000, 0x20000, 0x9c384e99 );
/*TODO*///		ROM_LOAD_ODD ( "tn2j-36.10f",  0xc0000, 0x20000, 0x4c4b2a0a );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "tn2-01m.3a",   0x000000, 0x80000, 0xcb950cf9 );
/*TODO*///		ROM_LOAD( "tn2-10m.3c",   0x080000, 0x80000, 0xa34ece70 );
/*TODO*///		ROM_LOAD( "tn2-02m.4a",   0x100000, 0x80000, 0xf2016a34 );
/*TODO*///		ROM_LOAD( "tn2-11m.4c",   0x180000, 0x80000, 0xd0edd30b );
/*TODO*///		ROM_LOAD( "tn2-03m.5a",   0x200000, 0x80000, 0x18a5bf59 );
/*TODO*///		ROM_LOAD( "tn2-12m.5c",   0x280000, 0x80000, 0xe04ff2f4 );
/*TODO*///		ROM_LOAD( "tn2-04m.6a",   0x300000, 0x80000, 0x094e0fb1 );
/*TODO*///		ROM_LOAD( "tn2-13m.6c",   0x380000, 0x80000, 0x426621c3 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "tn2j-09.12a",   0x00000, 0x08000, 0x6d8edcef );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "tn2j-18.11c",  0x00000, 0x20000, 0xa40bf9a7 );
/*TODO*///		ROM_LOAD( "tn2j-19.12c",  0x20000, 0x20000, 0x5b3b931e );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_pang3 = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "pa3w-17.11l",  0x00000, 0x80000, 0x12138234 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "pa3w-16.10l",  0x80000, 0x80000, 0xd1ba585c );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "pa3-01m.2c",    0x100000, 0x100000, 0x068a152c );
/*TODO*///		ROM_CONTINUE(              0x000000, 0x100000 );
/*TODO*///		ROM_LOAD( "pa3-07m.2f",    0x300000, 0x100000, 0x3a4a619d );
/*TODO*///		ROM_CONTINUE(              0x200000, 0x100000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "pa3-11.11f",    0x00000, 0x08000, 0x90a08c46 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "pa3-05.10d",    0x00000, 0x20000, 0x73a10d5d );
/*TODO*///		ROM_LOAD( "pa3-06.11d",    0x20000, 0x20000, 0xaffa4f82 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_pang3j = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "pa3j-17.11l",  0x00000, 0x80000, 0x21f6e51f );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "pa3j-16.10l",  0x80000, 0x80000, 0xca1d7897 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x400000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "pa3-01m.2c",    0x100000, 0x100000, 0x068a152c );
/*TODO*///		ROM_CONTINUE(              0x000000, 0x100000 );
/*TODO*///		ROM_LOAD( "pa3-07m.2f",    0x300000, 0x100000, 0x3a4a619d );
/*TODO*///		ROM_CONTINUE(              0x200000, 0x100000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "pa3-11.11f",    0x00000, 0x08000, 0x90a08c46 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "pa3-05.10d",    0x00000, 0x20000, 0x73a10d5d );
/*TODO*///		ROM_LOAD( "pa3-06.11d",    0x20000, 0x20000, 0xaffa4f82 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_megaman = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcma_23b.rom",   0x000000, 0x80000, 0x61e4a397 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcma_22b.rom",   0x080000, 0x80000, 0x708268c4 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcma_21a.rom",   0x100000, 0x80000, 0x4376ea95 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "rcm_03.rom",    0x000000, 0x80000, 0x36f3073c );
/*TODO*///		ROM_LOAD( "rcm_07.rom",    0x080000, 0x80000, 0x826de013 );
/*TODO*///		ROM_LOAD( "rcm_12.rom",    0x100000, 0x80000, 0xfed5f203 );
/*TODO*///		ROM_LOAD( "rcm_16.rom",    0x180000, 0x80000, 0x93d97fde );
/*TODO*///		ROM_LOAD( "rcm_01.rom",    0x200000, 0x80000, 0x6ecdf13f );
/*TODO*///		ROM_LOAD( "rcm_05.rom",    0x280000, 0x80000, 0x5dd131fd );
/*TODO*///		ROM_LOAD( "rcm_10.rom",    0x300000, 0x80000, 0x4dc8ada9 );
/*TODO*///		ROM_LOAD( "rcm_14.rom",    0x380000, 0x80000, 0x303be3bd );
/*TODO*///		ROM_LOAD( "rcm_04.rom",    0x400000, 0x80000, 0x54e622ff );
/*TODO*///		ROM_LOAD( "rcm_08.rom",    0x480000, 0x80000, 0xfbff64cf );
/*TODO*///		ROM_LOAD( "rcm_13.rom",    0x500000, 0x80000, 0x5069d4a9 );
/*TODO*///		ROM_LOAD( "rcm_17.rom",    0x580000, 0x80000, 0x92371042 );
/*TODO*///		ROM_LOAD( "rcm_02.rom",    0x600000, 0x80000, 0x944d4f0f );
/*TODO*///		ROM_LOAD( "rcm_06.rom",    0x680000, 0x80000, 0xf0faf813 );
/*TODO*///		ROM_LOAD( "rcm_11.rom",    0x700000, 0x80000, 0xf2b9ee06 );
/*TODO*///		ROM_LOAD( "rcm_15.rom",    0x780000, 0x80000, 0x4f2d372f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "rcm_09.rom",    0x00000, 0x08000, 0x9632d6ef );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "rcm_18.rom",    0x00000, 0x20000, 0x80f1f8aa );
/*TODO*///		ROM_LOAD( "rcm_19.rom",    0x20000, 0x20000, 0xf257dbe1 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_rockmanj = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcm23a.bin",   0x000000, 0x80000, 0xefd96cb2 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcm22a.bin",   0x080000, 0x80000, 0x8729a689 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "rcm21a.bin",   0x100000, 0x80000, 0x517ccde2 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "rcm_03.rom",    0x000000, 0x80000, 0x36f3073c );
/*TODO*///		ROM_LOAD( "rcm_07.rom",    0x080000, 0x80000, 0x826de013 );
/*TODO*///		ROM_LOAD( "rcm_12.rom",    0x100000, 0x80000, 0xfed5f203 );
/*TODO*///		ROM_LOAD( "rcm_16.rom",    0x180000, 0x80000, 0x93d97fde );
/*TODO*///		ROM_LOAD( "rcm_01.rom",    0x200000, 0x80000, 0x6ecdf13f );
/*TODO*///		ROM_LOAD( "rcm_05.rom",    0x280000, 0x80000, 0x5dd131fd );
/*TODO*///		ROM_LOAD( "rcm_10.rom",    0x300000, 0x80000, 0x4dc8ada9 );
/*TODO*///		ROM_LOAD( "rcm_14.rom",    0x380000, 0x80000, 0x303be3bd );
/*TODO*///		ROM_LOAD( "rcm_04.rom",    0x400000, 0x80000, 0x54e622ff );
/*TODO*///		ROM_LOAD( "rcm_08.rom",    0x480000, 0x80000, 0xfbff64cf );
/*TODO*///		ROM_LOAD( "rcm_13.rom",    0x500000, 0x80000, 0x5069d4a9 );
/*TODO*///		ROM_LOAD( "rcm_17.rom",    0x580000, 0x80000, 0x92371042 );
/*TODO*///		ROM_LOAD( "rcm_02.rom",    0x600000, 0x80000, 0x944d4f0f );
/*TODO*///		ROM_LOAD( "rcm_06.rom",    0x680000, 0x80000, 0xf0faf813 );
/*TODO*///		ROM_LOAD( "rcm_11.rom",    0x700000, 0x80000, 0xf2b9ee06 );
/*TODO*///		ROM_LOAD( "rcm_15.rom",    0x780000, 0x80000, 0x4f2d372f );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x28000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "rcm_09.rom",    0x00000, 0x08000, 0x9632d6ef );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x18000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "rcm_18.rom",    0x00000, 0x20000, 0x80f1f8aa );
/*TODO*///		ROM_LOAD( "rcm_19.rom",    0x20000, 0x20000, 0xf257dbe1 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	static RomLoadPtr rom_sfzch = new RomLoadPtr(){ public void handler(){ 
/*TODO*///		ROM_REGION( CODE_SIZE, REGION_CPU1 );     /* 68000 code */
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sfzch23",        0x000000, 0x80000, 0x1140743f );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sfza22",         0x080000, 0x80000, 0x8d9b2480 );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sfzch21",        0x100000, 0x80000, 0x5435225d );
/*TODO*///		ROM_LOAD_WIDE_SWAP( "sfza20",         0x180000, 0x80000, 0x806e8f38 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x800000, REGION_GFX1 | REGIONFLAG_DISPOSE );
/*TODO*///		ROM_LOAD( "sfz03",         0x000000, 0x80000, 0x9584ac85 );
/*TODO*///		ROM_LOAD( "sfz07",         0x080000, 0x80000, 0xbb2c734d );
/*TODO*///		ROM_LOAD( "sfz12",         0x100000, 0x80000, 0xf122693a );
/*TODO*///		ROM_LOAD( "sfz16",         0x180000, 0x80000, 0x19a5abd6 );
/*TODO*///		ROM_LOAD( "sfz01",         0x200000, 0x80000, 0x0dd53e62 );
/*TODO*///		ROM_LOAD( "sfz05",         0x280000, 0x80000, 0x2b47b645 );
/*TODO*///		ROM_LOAD( "sfz10",         0x300000, 0x80000, 0x2a7d675e );
/*TODO*///		ROM_LOAD( "sfz14",         0x380000, 0x80000, 0x09038c81 );
/*TODO*///		ROM_LOAD( "sfz04",         0x400000, 0x80000, 0xb983624c );
/*TODO*///		ROM_LOAD( "sfz08",         0x480000, 0x80000, 0x454f7868 );
/*TODO*///		ROM_LOAD( "sfz13",         0x500000, 0x80000, 0x7cf942c8 );
/*TODO*///		ROM_LOAD( "sfz17",         0x580000, 0x80000, 0x248b3b73 );
/*TODO*///		ROM_LOAD( "sfz02",         0x600000, 0x80000, 0x94c31e3f );
/*TODO*///		ROM_LOAD( "sfz06",         0x680000, 0x80000, 0x74fd9fb1 );
/*TODO*///		ROM_LOAD( "sfz11",         0x700000, 0x80000, 0xe35546c8 );
/*TODO*///		ROM_LOAD( "sfz15",         0x780000, 0x80000, 0x1aa17391 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x18000, REGION_CPU2 );/* 64k for the audio CPU (+banks) */
/*TODO*///		ROM_LOAD( "sfz09",         0x00000, 0x08000, 0xc772628b );
/*TODO*///		ROM_CONTINUE(              0x10000, 0x08000 );
/*TODO*///	
/*TODO*///		ROM_REGION( 0x40000, REGION_SOUND1 );/* Samples */
/*TODO*///		ROM_LOAD( "sfz18",         0x00000, 0x20000, 0x61022b2d );
/*TODO*///		ROM_LOAD( "sfz19",         0x20000, 0x20000, 0x3b5886d5 );
/*TODO*///	ROM_END(); }}; 
/*TODO*///	
/*TODO*///	
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_wof = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		wof_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_dino = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		dino_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_punisher = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		punisher_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_slammast = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		slammast_decode();
/*TODO*///	} };
/*TODO*///	
/*TODO*///	static public static InitDriverPtr init_pang3 = new InitDriverPtr() { public void handler() 
/*TODO*///	{
/*TODO*///		UBytePtr rom = memory_region(REGION_CPU1);
/*TODO*///		int A,src,dst;
/*TODO*///	
/*TODO*///		for (A = 0x80000;A < 0x100000;A += 2)
/*TODO*///		{
/*TODO*///			/* only the low 8 bits of each word are encrypted */
/*TODO*///			src = READ_WORD(&rom[A]);
/*TODO*///			dst = src & 0xff00;
/*TODO*///			if ((src & 0x01) != 0) dst ^= 0x04;
/*TODO*///			if ((src & 0x02) != 0) dst ^= 0x21;
/*TODO*///			if ((src & 0x04) != 0) dst ^= 0x01;
/*TODO*///			if (~src & 0x08) dst ^= 0x50;
/*TODO*///			if ((src & 0x10) != 0) dst ^= 0x40;
/*TODO*///			if ((src & 0x20) != 0) dst ^= 0x06;
/*TODO*///			if ((src & 0x40) != 0) dst ^= 0x08;
/*TODO*///			if (~src & 0x80) dst ^= 0x88;
/*TODO*///			WRITE_WORD(&rom[A],dst);
/*TODO*///		}
/*TODO*///	} };
/*TODO*///	
/*TODO*///	
	
	public static GameDriver driver_forgottn	   = new GameDriver("1988"	,"forgottn"	,"cps1.java"	,rom_forgottn,null	,machine_driver_forgottn	,input_ports_forgottn	,null	,ROT0	,	"Capcom", "Forgotten Worlds (US)" );
/*TODO*///	public static GameDriver driver_lostwrld	   = new GameDriver("1988"	,"lostwrld"	,"cps1.java"	,rom_lostwrld,driver_forgottn	,machine_driver_forgottn	,input_ports_forgottn	,null	,ROT0	,	"Capcom", "Lost Worlds (Japan)" )
	public static GameDriver driver_ghouls	   = new GameDriver("1988"	,"ghouls"	,"cps1.java"	,rom_ghouls,null	,machine_driver_cps1	,input_ports_ghouls	,null	,ROT0	,	"Capcom", "Ghouls'n Ghosts (World)" );
/*TODO*///	public static GameDriver driver_ghoulsu	   = new GameDriver("1988"	,"ghoulsu"	,"cps1.java"	,rom_ghoulsu,driver_ghouls	,machine_driver_cps1	,input_ports_ghouls	,null	,ROT0	,	"Capcom", "Ghouls'n Ghosts (US)" )
/*TODO*///	public static GameDriver driver_ghoulsj	   = new GameDriver("1988"	,"ghoulsj"	,"cps1.java"	,rom_ghoulsj,driver_ghouls	,machine_driver_cps1	,input_ports_ghouls	,null	,ROT0	,	"Capcom", "Dai Makai-Mura (Japan)" )
/*TODO*///	public static GameDriver driver_strider	   = new GameDriver("1989"	,"strider"	,"cps1.java"	,rom_strider,null	,machine_driver_cps1	,input_ports_strider	,null	,ROT0	,	"Capcom", "Strider (US)" )
/*TODO*///	public static GameDriver driver_striderj	   = new GameDriver("1989"	,"striderj"	,"cps1.java"	,rom_striderj,driver_strider	,machine_driver_cps1	,input_ports_strider	,null	,ROT0	,	"Capcom", "Strider Hiryu (Japan set 1)" )
/*TODO*///	public static GameDriver driver_stridrja	   = new GameDriver("1989"	,"stridrja"	,"cps1.java"	,rom_stridrja,driver_strider	,machine_driver_cps1	,input_ports_strider	,null	,ROT0	,	"Capcom", "Strider Hiryu (Japan set 2)" )
/*TODO*///	public static GameDriver driver_dwj	   = new GameDriver("1989"	,"dwj"	,"cps1.java"	,rom_dwj,null	,machine_driver_cps1	,input_ports_dwj	,null	,ROT0	,	"Capcom", "Tenchi wo Kurau (Japan)" )
/*TODO*///	public static GameDriver driver_willow	   = new GameDriver("1989"	,"willow"	,"cps1.java"	,rom_willow,null	,machine_driver_cps1	,input_ports_willow	,null	,ROT0	,	"Capcom", "Willow (Japan, English)" )
/*TODO*///	public static GameDriver driver_willowj	   = new GameDriver("1989"	,"willowj"	,"cps1.java"	,rom_willowj,driver_willow	,machine_driver_cps1	,input_ports_willow	,null	,ROT0	,	"Capcom", "Willow (Japan, Japanese)" )
/*TODO*///	public static GameDriver driver_unsquad	   = new GameDriver("1989"	,"unsquad"	,"cps1.java"	,rom_unsquad,null	,machine_driver_cps1	,input_ports_unsquad	,null	,ROT0	,	"Capcom", "U.N. Squadron (US)" )
/*TODO*///	public static GameDriver driver_area88	   = new GameDriver("1989"	,"area88"	,"cps1.java"	,rom_area88,driver_unsquad	,machine_driver_cps1	,input_ports_unsquad	,null	,ROT0	,	"Capcom", "Area 88 (Japan)" )
	public static GameDriver driver_ffight	   = new GameDriver("1989"	,"ffight"	,"cps1.java"	,rom_ffight,null	,machine_driver_cps1	,input_ports_ffight	,null	,ROT0	,	"Capcom", "Final Fight (World)" );
/*TODO*///	public static GameDriver driver_ffightu	   = new GameDriver("1989"	,"ffightu"	,"cps1.java"	,rom_ffightu,driver_ffight	,machine_driver_cps1	,input_ports_ffight	,null	,ROT0	,	"Capcom", "Final Fight (US)" )
/*TODO*///	public static GameDriver driver_ffightj	   = new GameDriver("1989"	,"ffightj"	,"cps1.java"	,rom_ffightj,driver_ffight	,machine_driver_cps1	,input_ports_ffight	,null	,ROT0	,	"Capcom", "Final Fight (Japan)" )
/*TODO*///	public static GameDriver driver_1941	   = new GameDriver("1990"	,"1941"	,"cps1.java"	,rom_1941,null	,machine_driver_cps1	,input_ports_1941	,null	,ROT270	,	"Capcom", "1941 - Counter Attack (World)" )
/*TODO*///	public static GameDriver driver_1941j	   = new GameDriver("1990"	,"1941j"	,"cps1.java"	,rom_1941j,driver_1941	,machine_driver_cps1	,input_ports_1941	,null	,ROT270	,	"Capcom", "1941 - Counter Attack (Japan)" )
/*TODO*///	public static GameDriver driver_mercs	   = new GameDriver("1990"	,"mercs"	,"cps1.java"	,rom_mercs,null	,machine_driver_cps1	,input_ports_mercs	,null	,ROT270	,	"Capcom", "Mercs (World)" )
/*TODO*///	public static GameDriver driver_mercsu	   = new GameDriver("1990"	,"mercsu"	,"cps1.java"	,rom_mercsu,driver_mercs	,machine_driver_cps1	,input_ports_mercs	,null	,ROT270	,	"Capcom", "Mercs (US)" )
/*TODO*///	public static GameDriver driver_mercsj	   = new GameDriver("1990"	,"mercsj"	,"cps1.java"	,rom_mercsj,driver_mercs	,machine_driver_cps1	,input_ports_mercs	,null	,ROT270	,	"Capcom", "Senjo no Ookami II (Japan)" )
/*TODO*///	public static GameDriver driver_mtwins	   = new GameDriver("1990"	,"mtwins"	,"cps1.java"	,rom_mtwins,null	,machine_driver_cps1	,input_ports_mtwins	,null	,ROT0	,	"Capcom", "Mega Twins (World)" )
/*TODO*///	public static GameDriver driver_chikij	   = new GameDriver("1990"	,"chikij"	,"cps1.java"	,rom_chikij,driver_mtwins	,machine_driver_cps1	,input_ports_mtwins	,null	,ROT0	,	"Capcom", "Chiki Chiki Boys (Japan)" )
/*TODO*///	public static GameDriver driver_msword	   = new GameDriver("1990"	,"msword"	,"cps1.java"	,rom_msword,null	,machine_driver_cps1	,input_ports_msword	,null	,ROT0	,	"Capcom", "Magic Sword - Heroic Fantasy (World)" )
/*TODO*///	public static GameDriver driver_mswordu	   = new GameDriver("1990"	,"mswordu"	,"cps1.java"	,rom_mswordu,driver_msword	,machine_driver_cps1	,input_ports_msword	,null	,ROT0	,	"Capcom", "Magic Sword - Heroic Fantasy (US)" )
/*TODO*///	public static GameDriver driver_mswordj	   = new GameDriver("1990"	,"mswordj"	,"cps1.java"	,rom_mswordj,driver_msword	,machine_driver_cps1	,input_ports_msword	,null	,ROT0	,	"Capcom", "Magic Sword (Japan)" )
/*TODO*///	public static GameDriver driver_cawing	   = new GameDriver("1990"	,"cawing"	,"cps1.java"	,rom_cawing,null	,machine_driver_cps1	,input_ports_cawing	,null	,ROT0	,	"Capcom", "Carrier Air Wing (World)" )
/*TODO*///	public static GameDriver driver_cawingj	   = new GameDriver("1990"	,"cawingj"	,"cps1.java"	,rom_cawingj,driver_cawing	,machine_driver_cps1	,input_ports_cawing	,null	,ROT0	,	"Capcom", "U.S. Navy (Japan)" )
/*TODO*///	public static GameDriver driver_nemo	   = new GameDriver("1990"	,"nemo"	,"cps1.java"	,rom_nemo,null	,machine_driver_cps1	,input_ports_nemo	,null	,ROT0	,	"Capcom", "Nemo (World)" )
/*TODO*///	public static GameDriver driver_nemoj	   = new GameDriver("1990"	,"nemoj"	,"cps1.java"	,rom_nemoj,driver_nemo	,machine_driver_cps1	,input_ports_nemo	,null	,ROT0	,	"Capcom", "Nemo (Japan)" )
/*TODO*///	public static GameDriver driver_sf2	   = new GameDriver("1991"	,"sf2"	,"cps1.java"	,rom_sf2,null	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (World 910214)" )
/*TODO*///	public static GameDriver driver_sf2a	   = new GameDriver("1991"	,"sf2a"	,"cps1.java"	,rom_sf2a,driver_sf2	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (US 910206)" )
/*TODO*///	public static GameDriver driver_sf2b	   = new GameDriver("1991"	,"sf2b"	,"cps1.java"	,rom_sf2b,driver_sf2	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (US 910214)" )
/*TODO*///	public static GameDriver driver_sf2e	   = new GameDriver("1991"	,"sf2e"	,"cps1.java"	,rom_sf2e,driver_sf2	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (US 910228)" )
/*TODO*///	public static GameDriver driver_sf2j	   = new GameDriver("1991"	,"sf2j"	,"cps1.java"	,rom_sf2j,driver_sf2	,machine_driver_sf2	,input_ports_sf2j	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (Japan 911210)" )
/*TODO*///	public static GameDriver driver_sf2jb	   = new GameDriver("1991"	,"sf2jb"	,"cps1.java"	,rom_sf2jb,driver_sf2	,machine_driver_sf2	,input_ports_sf2j	,null	,ROT0	,	"Capcom", "Street Fighter II - The World Warrior (Japan 910214)" )
/*TODO*///	public static GameDriver driver_3wonders	   = new GameDriver("1991"	,"3wonders"	,"cps1.java"	,rom_3wonders,null	,machine_driver_cps1	,input_ports_3wonders	,null	,ROT0_16BIT	,	"Capcom", "Three Wonders (US)" )
/*TODO*///	public static GameDriver driver_wonder3	   = new GameDriver("1991"	,"wonder3"	,"cps1.java"	,rom_wonder3,driver_3wonders	,machine_driver_cps1	,input_ports_3wonders	,null	,ROT0_16BIT	,	"Capcom", "Wonder 3 (Japan)" )
/*TODO*///	public static GameDriver driver_kod	   = new GameDriver("1991"	,"kod"	,"cps1.java"	,rom_kod,null	,machine_driver_cps1	,input_ports_kod	,null	,ROT0	,	"Capcom", "The King of Dragons (World)" )
/*TODO*///	public static GameDriver driver_kodj	   = new GameDriver("1991"	,"kodj"	,"cps1.java"	,rom_kodj,driver_kod	,machine_driver_cps1	,input_ports_kod	,null	,ROT0	,	"Capcom", "The King of Dragons (Japan)" )
/*TODO*///	public static GameDriver driver_kodb	   = new GameDriver("1991"	,"kodb"	,"cps1.java"	,rom_kodb,driver_kod	,machine_driver_cps1	,input_ports_kod	,null	,ROT0	,	"Capcom", "The King of Dragons (bootleg)", GAME_NOT_WORKING )
/*TODO*///	public static GameDriver driver_captcomm	   = new GameDriver("1991"	,"captcomm"	,"cps1.java"	,rom_captcomm,null	,machine_driver_cps1	,input_ports_captcomm	,null	,ROT0_16BIT	,	"Capcom", "Captain Commando (World)" )
/*TODO*///	public static GameDriver driver_captcomu	   = new GameDriver("1991"	,"captcomu"	,"cps1.java"	,rom_captcomu,driver_captcomm	,machine_driver_cps1	,input_ports_captcomm	,null	,ROT0_16BIT	,	"Capcom", "Captain Commando (US)" )
/*TODO*///	public static GameDriver driver_captcomj	   = new GameDriver("1991"	,"captcomj"	,"cps1.java"	,rom_captcomj,driver_captcomm	,machine_driver_cps1	,input_ports_captcomm	,null	,ROT0_16BIT	,	"Capcom", "Captain Commando (Japan)" )
/*TODO*///	public static GameDriver driver_knights	   = new GameDriver("1991"	,"knights"	,"cps1.java"	,rom_knights,null	,machine_driver_cps1	,input_ports_knights	,null	,ROT0_16BIT	,	"Capcom", "Knights of the Round (World)" )
/*TODO*///	public static GameDriver driver_knightsu	   = new GameDriver("1991"	,"knightsu"	,"cps1.java"	,rom_knightsu,driver_knights	,machine_driver_cps1	,input_ports_knights	,null	,ROT0_16BIT	,	"Capcom", "Knights of the Round (US)" )
/*TODO*///	public static GameDriver driver_knightsj	   = new GameDriver("1991"	,"knightsj"	,"cps1.java"	,rom_knightsj,driver_knights	,machine_driver_cps1	,input_ports_knights	,null	,ROT0_16BIT	,	"Capcom", "Knights of the Round (Japan)" )
/*TODO*///	public static GameDriver driver_sf2ce	   = new GameDriver("1992"	,"sf2ce"	,"cps1.java"	,rom_sf2ce,null	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II' - Champion Edition (World)" )
/*TODO*///	public static GameDriver driver_sf2cea	   = new GameDriver("1992"	,"sf2cea"	,"cps1.java"	,rom_sf2cea,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II' - Champion Edition (US rev A)" )
/*TODO*///	public static GameDriver driver_sf2ceb	   = new GameDriver("1992"	,"sf2ceb"	,"cps1.java"	,rom_sf2ceb,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II' - Champion Edition (US rev B)" )
/*TODO*///	public static GameDriver driver_sf2cej	   = new GameDriver("1992"	,"sf2cej"	,"cps1.java"	,rom_sf2cej,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2j	,null	,ROT0	,	"Capcom", "Street Fighter II' - Champion Edition (Japan)" )
/*TODO*///	public static GameDriver driver_sf2rb	   = new GameDriver("1992"	,"sf2rb"	,"cps1.java"	,rom_sf2rb,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"hack",  "Street Fighter II' - Champion Edition (Rainbow)" )
/*TODO*///	public static GameDriver driver_sf2red	   = new GameDriver("1992"	,"sf2red"	,"cps1.java"	,rom_sf2red,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"hack",  "Street Fighter II' - Champion Edition (Red Wave)" )
/*TODO*///	public static GameDriver driver_sf2accp2	   = new GameDriver("1992"	,"sf2accp2"	,"cps1.java"	,rom_sf2accp2,driver_sf2ce	,machine_driver_sf2accp2	,input_ports_sf2	,null	,ROT0	,	"hack",  "Street Fighter II' - Champion Edition (Accelerator Pt.II)" )
/*TODO*///	public static GameDriver driver_varth	   = new GameDriver("1992"	,"varth"	,"cps1.java"	,rom_varth,null	,machine_driver_cps1	,input_ports_varth	,null	,ROT270	,	"Capcom", "Varth - Operation Thunderstorm (World)" )
/*TODO*///	public static GameDriver driver_varthu	   = new GameDriver("1992"	,"varthu"	,"cps1.java"	,rom_varthu,driver_varth	,machine_driver_cps1	,input_ports_varth	,null	,ROT270	,	"Capcom (Romstar license)", "Varth - Operation Thunderstorm (US)" )
/*TODO*///	public static GameDriver driver_varthj	   = new GameDriver("1992"	,"varthj"	,"cps1.java"	,rom_varthj,driver_varth	,machine_driver_cps1	,input_ports_varth	,null	,ROT270	,	"Capcom", "Varth - Operation Thunderstorm (Japan)" )
/*TODO*///	public static GameDriver driver_cworld2j	   = new GameDriver("1992"	,"cworld2j"	,"cps1.java"	,rom_cworld2j,null	,machine_driver_cps1	,input_ports_cworld2j	,null	,ROT0_16BIT	,	"Capcom", "Capcom World 2 (Japan)" )
/*TODO*///	public static GameDriver driver_sf2t	   = new GameDriver("1992"	,"sf2t"	,"cps1.java"	,rom_sf2t,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2	,null	,ROT0	,	"Capcom", "Street Fighter II' - Hyper Fighting (US)" )
/*TODO*///	public static GameDriver driver_sf2tj	   = new GameDriver("1992"	,"sf2tj"	,"cps1.java"	,rom_sf2tj,driver_sf2ce	,machine_driver_sf2	,input_ports_sf2j	,null	,ROT0	,	"Capcom", "Street Fighter II' Turbo - Hyper Fighting (Japan)" )
/*TODO*///	public static GameDriver driver_pnickj	   = new GameDriver("1994"	,"pnickj"	,"cps1.java"	,rom_pnickj,null	,machine_driver_cps1	,input_ports_pnickj	,null	,ROT0	,	"Capcom (licensed from Compile)", "Pnickies (Japan)" )
/*TODO*///	public static GameDriver driver_qad	   = new GameDriver("1992"	,"qad"	,"cps1.java"	,rom_qad,null	,machine_driver_cps1	,input_ports_qad	,null	,ROT0	,	"Capcom", "Quiz & Dragons (US)" )
/*TODO*///	public static GameDriver driver_qadj	   = new GameDriver("1994"	,"qadj"	,"cps1.java"	,rom_qadj,driver_qad	,machine_driver_cps1	,input_ports_qadj	,null	,ROT0	,	"Capcom", "Quiz & Dragons (Japan)" )
/*TODO*///	public static GameDriver driver_qtono2	   = new GameDriver("1995"	,"qtono2"	,"cps1.java"	,rom_qtono2,null	,machine_driver_cps1	,input_ports_qtono2	,null	,ROT0	,	"Capcom", "Quiz Tonosama no Yabou 2 Zenkoku-ban (Japan)" )
/*TODO*///	public static GameDriver driver_megaman	   = new GameDriver("1995"	,"megaman"	,"cps1.java"	,rom_megaman,null	,machine_driver_cps1	,input_ports_megaman	,null	,ROT0_16BIT	,	"Capcom", "Mega Man - The Power Battle (Asia)" )
/*TODO*///	public static GameDriver driver_rockmanj	   = new GameDriver("1995"	,"rockmanj"	,"cps1.java"	,rom_rockmanj,driver_megaman	,machine_driver_cps1	,input_ports_megaman	,null	,ROT0_16BIT	,	"Capcom", "Rockman - The Power Battle (Japan)" )
/*TODO*///	public static GameDriver driver_sfzch	   = new GameDriver("1995"	,"sfzch"	,"cps1.java"	,rom_sfzch,null	,machine_driver_cps1	,input_ports_sfzch	,null	,ROT0_16BIT	,	"Capcom", "Street Fighter ZERO (Japan CPS Changer)" )
/*TODO*///	
/*TODO*///	public static GameDriver driver_wof	   = new GameDriver("1992"	,"wof"	,"cps1.java"	,rom_wof,null	,machine_driver_qsound	,input_ports_wof	,init_wof	,ROT0	,	"Capcom", "Warriors of Fate (World)" )
/*TODO*///	public static GameDriver driver_wofa	   = new GameDriver("1992"	,"wofa"	,"cps1.java"	,rom_wofa,driver_wof	,machine_driver_qsound	,input_ports_wof	,init_wof	,ROT0	,	"Capcom", "Sangokushi II (Asia)" )
/*TODO*///	public static GameDriver driver_wofj	   = new GameDriver("1992"	,"wofj"	,"cps1.java"	,rom_wofj,driver_wof	,machine_driver_qsound	,input_ports_wof	,init_wof	,ROT0	,	"Capcom", "Tenchi wo Kurau II - Sekiheki no Tatakai (Japan)" )
/*TODO*///	public static GameDriver driver_dino	   = new GameDriver("1993"	,"dino"	,"cps1.java"	,rom_dino,null	,machine_driver_qsound	,input_ports_dino	,init_dino	,ROT0	,	"Capcom", "Cadillacs and Dinosaurs (World)" )
/*TODO*///	public static GameDriver driver_dinoj	   = new GameDriver("1993"	,"dinoj"	,"cps1.java"	,rom_dinoj,driver_dino	,machine_driver_qsound	,input_ports_dino	,init_dino	,	,	ROT0,       "Capcom", "Cadillacs Kyouryuu-Shinseiki (Japan)" )
/*TODO*///	public static GameDriver driver_punisher	   = new GameDriver("1993"	,"punisher"	,"cps1.java"	,rom_punisher,null	,machine_driver_qsound	,input_ports_punisher	,init_punisher	,ROT0	,	"Capcom", "The Punisher (World)" )
/*TODO*///	public static GameDriver driver_punishru	   = new GameDriver("1993"	,"punishru"	,"cps1.java"	,rom_punishru,driver_punisher	,machine_driver_qsound	,input_ports_punisher	,init_punisher	,ROT0	,	"Capcom", "The Punisher (US)" )
/*TODO*///	public static GameDriver driver_punishrj	   = new GameDriver("1993"	,"punishrj"	,"cps1.java"	,rom_punishrj,driver_punisher	,machine_driver_qsound	,input_ports_punisher	,init_punisher	,ROT0	,	"Capcom", "The Punisher (Japan)" )
/*TODO*///	public static GameDriver driver_slammast	   = new GameDriver("1993"	,"slammast"	,"cps1.java"	,rom_slammast,null	,machine_driver_qsound	,input_ports_slammast	,init_slammast	,ROT0_16BIT	,	"Capcom", "Saturday Night Slam Masters (World)" )
/*TODO*///	public static GameDriver driver_slammasu	   = new GameDriver("1993"	,"slammasu"	,"cps1.java"	,rom_slammasu,driver_slammast	,machine_driver_qsound	,input_ports_slammast	,init_slammast	,ROT0_16BIT	,	"Capcom", "Saturday Night Slam Masters (US)" )
/*TODO*///	public static GameDriver driver_mbomberj	   = new GameDriver("1993"	,"mbomberj"	,"cps1.java"	,rom_mbomberj,driver_slammast	,machine_driver_qsound	,input_ports_slammast	,init_slammast	,ROT0_16BIT	,	"Capcom", "Muscle Bomber - The Body Explosion (Japan)" )
/*TODO*///	public static GameDriver driver_mbombrd	   = new GameDriver("1993"	,"mbombrd"	,"cps1.java"	,rom_mbombrd,driver_slammast	,machine_driver_qsound	,input_ports_slammast	,init_slammast	,ROT0_16BIT	,	"Capcom", "Muscle Bomber Duo - Ultimate Team Battle (World)" )
/*TODO*///	public static GameDriver driver_mbombrdj	   = new GameDriver("1993"	,"mbombrdj"	,"cps1.java"	,rom_mbombrdj,driver_slammast	,machine_driver_qsound	,input_ports_slammast	,init_slammast	,ROT0_16BIT	,	"Capcom", "Muscle Bomber Duo - Heat Up Warriors (Japan)" )
/*TODO*///	
/*TODO*///	/* Japanese version of Pang 3 is encrypted, Euro version is not */
/*TODO*///	public static GameDriver driver_pang3	   = new GameDriver("1995"	,"pang3"	,"cps1.java"	,rom_pang3,null	,machine_driver_pang3	,input_ports_pang3	,null	,ROT0_16BIT	,	"Mitchell", "Pang! 3 (Euro)" )
/*TODO*///	public static GameDriver driver_pang3j	   = new GameDriver("1995"	,"pang3j"	,"cps1.java"	,rom_pang3j,driver_pang3	,machine_driver_pang3	,input_ports_pang3	,init_pang3	,ROT0_16BIT	,	"Mitchell", "Pang! 3 (Japan)" )
	
	
}
