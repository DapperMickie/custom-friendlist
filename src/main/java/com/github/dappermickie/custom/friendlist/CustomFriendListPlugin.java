package com.github.dappermickie.custom.friendlist;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import static net.runelite.api.MenuAction.ITEM_USE_ON_PLAYER;
import static net.runelite.api.MenuAction.PLAYER_EIGHTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FIFTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FIRST_OPTION;
import static net.runelite.api.MenuAction.PLAYER_FOURTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SECOND_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SEVENTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_SIXTH_OPTION;
import static net.runelite.api.MenuAction.PLAYER_THIRD_OPTION;
import static net.runelite.api.MenuAction.RUNELITE_PLAYER;
import static net.runelite.api.MenuAction.WALK;
import static net.runelite.api.MenuAction.WIDGET_TARGET_ON_PLAYER;
import net.runelite.api.MenuEntry;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.ScriptID;
import net.runelite.api.WorldType;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;

@Slf4j
@PluginDescriptor(
	name = "Odablock DMM Finale"
)
public class CustomFriendListPlugin extends Plugin
{
	@Inject
	private Client client;

	/**
	 * CUSTOM FRIEND LIST -- ENTITY HIDER
	 **/

	@Inject
	private CustomFriendListConfig config;

	@Inject
	private Hooks hooks;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;
	private boolean hideOthers;
	private boolean hideOthers2D;

	/**
	 * CUSTOM FRIEND LIST -- PLAYER INDICATOR
	 **/

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private CustomFriendListIndicatorsOverlay playerIndicatorsOverlay;

	@Inject
	private CustomFriendListIndicatorsTileOverlay playerIndicatorsTileOverlay;

	@Inject
	private CustomFriendListIndicatorsMinimapOverlay playerIndicatorsMinimapOverlay;

	@Inject
	private CustomFriendListIndicatorsService playerIndicatorsService;

	@Inject
	private ClientThread clientThread;

	private static final String TRADING_WITH_TEXT = "Trading with: ";

	private boolean isDmmWorld = false;

	private static final Set<String> playerList = Set.of(
		"7 of october",
		"Ai Odablock",
		"aRushaholic",
		"BabyGoliath",
		"big re ups",
		"Cwadeiron",
		"D52",
		"D Maker",
		"GlM FIllILIl",
		"H20Zz",
		"IronFBN",
		"J O L T I K",
		"Koreana",
		"L00k WEST",
		"Mage Sat U",
		"Mezitry",
		"Natsu DMM",
		"Papi Goat",
		"Pinhead Jim",
		"Pure Rxpist",
		"qv7",
		"rip2pk",
		"SassanechTTV",
		"SHXNCED",
		"Skyapunk",
		"Superior Fo",
		"Tim Horton",
		"TNSTN5TNSTNS",
		"w ei",
		"Z otiyac",
		"Zealots Maul",
		"09109109",
		"0sB2teamDMM",
		"109NORTHWEST",
		"126 american",
		"1 Hurts",
		"1Tick Wavy",
		"1vz",
		"29 th",
		"337 Problems",
		"3xhausted",
		"450lbs fatso",
		"45 Def Tyler",
		"4bop",
		"6Platinum9",
		"70EASTWEST",
		"73 Diesel",
		"A15",
		"A Big Parrot",
		"A Fu Fu",
		"A riel",
		"Aaron2As",
		"ABCDEEZ NUTS",
		"abzdullaa",
		"AFUFUFUUFUFF",
		"Ahkyou B2",
		"Airbornee",
		"alex rsps",
		"ALEXJ0NES",
		"Amarok Moon",
		"AMonk",
		"Astr00",
		"Avatar Zuko",
		"B4NDIIIIIIIT",
		"B E NNN",
		"Baby Dingy",
		"Baby Remedy",
		"Babydile Jr",
		"BAD IMPULSES",
		"barn muck",
		"bb w9",
		"BehindY0u97",
		"bestgim",
		"BFM Chew",
		"bigwanggang6",
		"Blue Jesus",
		"BlueberyKush",
		"Bo Bowen",
		"Bossly",
		"Brandybuck",
		"BriCep1",
		"BTM GANG",
		"Build 1 Boss",
		"BUY SPY",
		"Cb Lvl 70",
		"CDQ",
		"chalk zone",
		"CheckMateIn2",
		"ChickWitDlCK",
		"CHlCKNUGGET",
		"ChowhallVet",
		"Ckzee",
		"CM MikeNolan",
		"coifmaster",
		"Colefax Ave",
		"Collins Void",
		"CrunchyNut7",
		"Crystalizing",
		"D00LAS",
		"D3THR0NE",
		"d4 m3d1c",
		"D R0SE 600",
		"D Y W C",
		"Dab 710 life",
		"Dank Kingdom",
		"davidwells6",
		"ddep",
		"Dead Fresh",
		"demon pack",
		"dmm h2o",
		"DMM Junior",
		"DMM Q",
		"Don Lars",
		"Dr Bear Bear",
		"dr vinegar",
		"DRAGONHlDES",
		"DROPTHELOOTZ",
		"Druc",
		"DrunkenMasta",
		"Dusted",
		"Easy Game",
		"EAT NEON",
		"EBK Thuggish",
		"EdgeYouOff",
		"Edhanjo",
		"EGIRLCEO",
		"eiflA",
		"eJuice",
		"Elder B1",
		"Elvato Lenny",
		"Emma Hixx",
		"enter rektum",
		"EternalXodia",
		"F2OC",
		"Failbrid HC",
		"Failsauce Jr",
		"Fe Graceful",
		"Fight Mi lk",
		"Fk the Rats",
		"Flappey",
		"FlNS",
		"FreshFroge",
		"Fuertejuann",
		"Fukn Demon",
		"G Chip",
		"g haul",
		"Garb Alt",
		"Garnish",
		"Get BIZI",
		"GetOffMee",
		"gFe FatalGTR",
		"Gifed",
		"GIM 0548Boon",
		"GIM Achilles",
		"GIM Goated",
		"GIM IN DEBT",
		"GIM PRESIDNT",
		"gimCowSlayer",
		"Ginnog",
		"Gmge",
		"Go Vegan",
		"goblin hat",
		"h anzolo",
		"hackedsadge",
		"HairyPotion",
		"HAKUNA AMANI",
		"Hatto Jr",
		"hawk putuh",
		"hcgenes",
		"HELVPERKELE",
		"HesEast",
		"HidingInLore",
		"Higher Iron",
		"Hiori Yo",
		"Hold Da Maul",
		"holdmywood69",
		"Honor Pking",
		"HoodedTurd",
		"HorgCore",
		"Huntz0rz",
		"Hussiee",
		"i am new",
		"I Execute I",
		"i needa tank",
		"IamPeppered",
		"Ice Barraqe",
		"IDF SUCKS",
		"idkwid12",
		"iicy",
		"IlIlXXlIlI",
		"im45s",
		"Imperialcat9",
		"iPhone Hanni",
		"IPlayHigh",
		"iron cc978",
		"iron moodmad",
		"Iron Smaam",
		"IronRiskit",
		"iruinurgirl",
		"izeewenot",
		"J0hnFK3nn3dy",
		"J backpack",
		"Jabloyd",
		"Jackoo H",
		"Jacurutu",
		"Jelotin",
		"Jllkken",
		"JoshTempz",
		"Joshy9519193",
		"Joss m8",
		"Kamikaze 64",
		"Key Rogue",
		"kiipeilyteli",
		"Killclaw868",
		"Kincade",
		"Kingdavid298",
		"Klovni",
		"KOTJ",
		"KristenRS",
		"Krytab",
		"Ky zrr",
		"L A B",
		"L ok i",
		"landoctor",
		"Lay",
		"LeapingRhino",
		"LethalTomCat",
		"Leviosah",
		"lil axe",
		"Lil Vanilla",
		"lindyman",
		"Linux Shill",
		"lmmola",
		"loonisdog7",
		"lSRAHELL",
		"Lupier",
		"M2L Nols",
		"M 4 RS",
		"M y n x i",
		"madbombermo",
		"mantariin",
		"Marrakechh",
		"masorvaa",
		"Maus Meister",
		"Meatformoms",
		"Mexican OT",
		"MF jp",
		"MiissMandii",
		"milagros0",
		"Mojo Arts",
		"MoldyBaldy",
		"Mon k",
		"Monkey D Imp",
		"Mr Stev",
		"Mr Whiskers8",
		"Mt Cox",
		"MulikkaGap",
		"MVPaul",
		"My Fireburn",
		"MyPknLvlIs98",
		"MySmellyD",
		"Natte Eland",
		"Navenger",
		"nice good54",
		"Nick from MF",
		"NickyPGH",
		"NineDollars",
		"NNNNOOOONNNN",
		"Normal Man69",
		"Not Babe",
		"NOT SIUWAX",
		"Not ZB",
		"Odas Glizzy",
		"ODD FLOX",
		"OG Ports",
		"Olddmmbetter",
		"OmarTheBald",
		"ome22",
		"One Last Ko",
		"Pack Yarack",
		"pipapilletje",
		"PKalimeyo",
		"PKDANOOBS",
		"Pker NL 16",
		"Plankton 69",
		"PlayerVsNPC",
		"pot z",
		"PT O",
		"Pure Butter",
		"PurenPoikane",
		"PureR4ngeDom",
		"Pusi Kurac",
		"Quest4Nerds",
		"qw8",
		"RADEON6900XT",
		"RanarrWater",
		"Rat Like",
		"RedWave24",
		"Redzie1",
		"Rhino dawg",
		"Roided",
		"ROofDmm",
		"rorw",
		"RtoRumble",
		"S2k Julian",
		"S T R O N T",
		"SaccaDMM",
		"Salted Slug",
		"Sankz",
		"Seealer",
		"SHA4AA44A4RK",
		"Shagzy",
		"ShajMaster12",
		"Shalaria",
		"sheepboy666",
		"Shinshomper",
		"SilkyJonson",
		"Slaps Nuts",
		"slizzys",
		"Slow2K",
		"Smoked",
		"splity0wig",
		"SpraynPRA",
		"Steppage",
		"stevie main",
		"stink rick",
		"StolenBeans",
		"STOP RUNNlNG",
		"SUB 50 KILLA",
		"SullyProc",
		"swagzero",
		"Swappy7",
		"T er",
		"Tanky Pixels",
		"tap tap",
		"tatakae zx",
		"tb j",
		"TCENNO DISC",
		"Tethryx",
		"The 1 cm guy",
		"The 99 Pure",
		"TheOldSouth",
		"tiliDMM",
		"TimmaysHere",
		"Titoqt",
		"toiletplung8",
		"ToKeN TeRRoR",
		"tokinieks121",
		"Tome Risk",
		"Tommy Pajamy",
		"tonenick",
		"Tourge",
		"Trkizlobanje",
		"TrumpHadRol",
		"TYKElSHA",
		"UFC Golf",
		"UnidGuam",
		"Unx",
		"Uri tart",
		"urself twice",
		"UZl",
		"Valgein B2",
		"VLD Zoggy",
		"Vollice",
		"Walkout",
		"Wallzaper",
		"Wanheda Jr",
		"wasgudlilbro",
		"WB9",
		"wfmwgwgwgwgw",
		"Whizalicious",
		"Wienterr",
		"wienterrr",
		"WlIW",
		"wocktris",
		"wokingham g",
		"WON DMM",
		"Worcestersur",
		"WPA",
		"Wright Main",
		"x 0lev x",
		"x GetLucky x",
		"xLOSTxKAUSEx",
		"Y0 M A M A",
		"YelloThunda",
		"Yh Shuhua",
		"YSG PURE DMM",
		"Zaleerion",
		"Zephyrind",
		"Zhats Zrazy",
		"ZIGACHAD",
		"Zongz",
		"b66",
		"NeverLoseBro",
		"09O",
		"1 tick stfd",
		"6 hrs",
		"Alexvndre",
		"ALMOST11HP",
		"Altarism",
		"Best Pker",
		"Bk 2 Lumb M8",
		"buddy pieces",
		"chipdumbster",
		"corb on orb",
		"Cr4ck3d G0at",
		"Da Sniper007",
		"DINO SUCKS D",
		"DMM Worker99",
		"Doobes",
		"Dude I Rush",
		"H I C H E W",
		"Ha yden",
		"Hcscrewup2",
		"heckin pure",
		"HEPATlTlS B",
		"HeyThere",
		"im dillo",
		"ju sto",
		"LANAFROMWISH",
		"LegendSmokes",
		"lithpytommy",
		"Mashuugs",
		"Nvr Ironman",
		"Ojibuh",
		"pc7",
		"RobPandaDMM",
		"rolll",
		"sumer",
		"TaintPainta",
		"tank3d oxy",
		"TBNRflamingo",
		"The On3",
		"Thirty5 Euro",
		"TL9daysLate",
		"VI0LENT",
		"WE T0DD DID",
		"WildBaws",
		"Zee Kevin",
		"Haram Lord");

	@Override
	protected void startUp()
	{
		// Entity hider
		updateConfig();

		if (client.getGameState().equals(GameState.LOGGED_IN))
		{
			updateRequestTick = client.getTickCount();
		}
	}

	@Override
	protected void shutDown()
	{
		// Entity Hider
		hooks.unregisterRenderableDrawListener(drawListener);

		// Player indicator
		overlayManager.remove(playerIndicatorsOverlay);
		overlayManager.remove(playerIndicatorsTileOverlay);
		overlayManager.remove(playerIndicatorsMinimapOverlay);
	}

	private void updateOverlays()
	{
		isDmmWorld = (client.getWorldType().contains(WorldType.SEASONAL) || client.getWorldType().contains(WorldType.TOURNAMENT_WORLD))
			&& client.getWorldType().contains(WorldType.DEADMAN);

		// Entity Hider
		hooks.unregisterRenderableDrawListener(drawListener);

		// Player indicator
		overlayManager.remove(playerIndicatorsOverlay);
		overlayManager.remove(playerIndicatorsTileOverlay);
		overlayManager.remove(playerIndicatorsMinimapOverlay);

		if (isDmmWorld && isPlayerInList(client.getLocalPlayer().getName()))
		{
			// Entity hider
			updateConfig();

			hooks.registerRenderableDrawListener(drawListener);

			// Player indicator
			overlayManager.add(playerIndicatorsOverlay);
			overlayManager.add(playerIndicatorsTileOverlay);
			overlayManager.add(playerIndicatorsMinimapOverlay);
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateRequestTick = client.getTickCount();
		}
	}

	private void updateConfig()
	{
		hideOthers = config.hideOthers();
		hideOthers2D = config.hideOthers2D();
	}

	@VisibleForTesting
	boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		if (renderable instanceof Player)
		{
			Player player = (Player) renderable;
			Player local = client.getLocalPlayer();

			if (player.getName() == null)
			{
				// player.isFriend() and player.isFriendsChatMember() npe when the player has a null name
				return true;
			}

			// Allow hiding local self in pvp, which is an established meta.
			// It is more advantageous than renderself due to being able to still render local player 2d
			if (player == local)
			{
				return true;
			}

			if (isPlayerInList(player.getName()))
			{
				return !(drawingUI ? hideOthers2D : hideOthers);
			}

			return true;
		}

		return true;
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (client.getTickCount() <= 0 || updateRequestTick == -1)
		{
			return;
		}

		if (client.getTickCount() > updateRequestTick + 2)
		{
			updateOverlays();
			updateRequestTick = -1;
		}
	}

	private int updateRequestTick = -1;

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (client.isMenuOpen())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();

		for (MenuEntry entry : menuEntries)
		{
			MenuAction type = entry.getType();

			if (type == WALK
				|| type == WIDGET_TARGET_ON_PLAYER
				|| type == ITEM_USE_ON_PLAYER
				|| type == PLAYER_FIRST_OPTION
				|| type == PLAYER_SECOND_OPTION
				|| type == PLAYER_THIRD_OPTION
				|| type == PLAYER_FOURTH_OPTION
				|| type == PLAYER_FIFTH_OPTION
				|| type == PLAYER_SIXTH_OPTION
				|| type == PLAYER_SEVENTH_OPTION
				|| type == PLAYER_EIGHTH_OPTION
				|| type == RUNELITE_PLAYER)
			{
				Player[] players = client.getCachedPlayers();
				Player player = null;

				int identifier = entry.getIdentifier();

				// 'Walk here' identifiers are offset by 1 because the default
				// identifier for this option is 0, which is also a player index.
				if (type == WALK)
				{
					identifier--;
				}

				if (identifier >= 0 && identifier < players.length)
				{
					player = players[identifier];
				}

				if (player == null)
				{
					continue;
				}

				CustomFriendListIndicatorsService.Decorations decorations = playerIndicatorsService.getDecorations(player);
				if (decorations == null)
				{
					continue;
				}

				String oldTarget = entry.getTarget();
				String newTarget = decorateTarget(oldTarget, decorations);

				entry.setTarget(newTarget);
			}
		}
	}

	@VisibleForTesting
	String decorateTarget(String oldTarget, CustomFriendListIndicatorsService.Decorations decorations)
	{
		String newTarget = oldTarget;

		if (decorations.getColor() != null)
		{
			String prefix = "";
			int idx = oldTarget.indexOf("->");
			if (idx != -1)
			{
				prefix = oldTarget.substring(0, idx + 3); // <col=ff9040>Earth rune</col><col=ff> ->
				oldTarget = oldTarget.substring(idx + 3);
			}

			// <col=ff0000>title0RuneLitetitle1<col=ff>  (level-126)title2
			idx = oldTarget.indexOf('>');
			// remove leading <col>
			oldTarget = oldTarget.substring(idx + 1);

			newTarget = prefix + ColorUtil.prependColorTag(oldTarget, decorations.getColor());
		}

		return newTarget;
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.TRADE_MAIN_INIT)
		{
			clientThread.invokeLater(() ->
			{
				Widget tradeTitle = client.getWidget(ComponentID.TRADE_HEADER);
				String header = tradeTitle.getText();
				String playerName = header.substring(TRADING_WITH_TEXT.length());

				Player targetPlayer = findPlayer(playerName);
				if (targetPlayer == null)
				{
					return;
				}

				CustomFriendListIndicatorsService.Decorations decorations = playerIndicatorsService.getDecorations(targetPlayer);
				if (decorations != null && decorations.getColor() != null)
				{
					tradeTitle.setText(TRADING_WITH_TEXT + ColorUtil.wrapWithColorTag(playerName, decorations.getColor()));
				}
			});
		}
	}

	public boolean isPlayerInList(String name)
	{
		if (name == null)
		{
			return false;
		}
		return playerList.contains(name);
	}

	private Player findPlayer(String name)
	{
		for (Player player : client.getPlayers())
		{
			if (player.getName().equals(name))
			{
				return player;
			}
		}
		return null;
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e)
	{
		if (e.getGroup().equals("odablockdmmfinale"))
		{
			updateConfig();
		}
	}

	@Provides
	CustomFriendListConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CustomFriendListConfig.class);
	}
}
