/*
 * Copyright 2020 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.formdev.flatlaf.testing.uidefaults;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIDefaults.ActiveValue;
import javax.swing.UIDefaults.LazyValue;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicLookAndFeel;
import com.formdev.flatlaf.*;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.util.StringUtils;
import com.formdev.flatlaf.util.SystemInfo;

/**
 * Dumps look and feel UI defaults to files.
 *
 * @author Karl Tauber
 */
public class UIDefaultsDump
{
	private final LookAndFeel lookAndFeel;
	private final UIDefaults defaults;

	private String lastPrefix;
	private JComponent dummyComponent;

	public static void main( String[] args ) {
		System.setProperty( "sun.java2d.uiScale", "1x" );
		System.setProperty( "flatlaf.uiScale", "1x" );

		File dir = new File( "src/main/resources/com/formdev/flatlaf/testing/uidefaults" );

		dump( FlatLightLaf.class.getName(), dir );
		dump( FlatDarkLaf.class.getName(), dir );

//		dump( FlatIntelliJLaf.class.getName(), dir );
//		dump( FlatDarculaLaf.class.getName(), dir );
//
//		dump( MyBasicLookAndFeel.class.getName(), dir );
//		dump( MetalLookAndFeel.class.getName(), dir );
//		dump( NimbusLookAndFeel.class.getName(), dir );
//
//		if( SystemInfo.IS_WINDOWS )
//			dump( "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", dir );
//		else if( SystemInfo.IS_MAC )
//			dump( "com.apple.laf.AquaLookAndFeel", dir );
//
//		dump( "com.jgoodies.looks.plastic.PlasticLookAndFeel", dir );
//		dump( "com.jgoodies.looks.windows.WindowsLookAndFeel", dir );
//		dump( "com.alee.laf.WebLookAndFeel", dir );
//		try {
//			EventQueue.invokeAndWait( () -> {
//				dump( "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel", dir );
//			} );
//		} catch( Exception ex ) {
//			// TODO Auto-generated catch block
//			ex.printStackTrace();
//		}

//		dumpIntelliJThemes( dir );
	}

	@SuppressWarnings( "unused" )
	private static void dumpIntelliJThemes( File dir ) {
		dir = new File( dir, "intellijthemes" );

		for( LookAndFeelInfo info : FlatAllIJThemes.INFOS ) {
			String lafClassName = info.getClassName();
			String relativeLafClassName = StringUtils.removeLeading( lafClassName, "com.formdev.flatlaf.intellijthemes." );
			File dir2 = relativeLafClassName.lastIndexOf( '.' ) >= 0
				? new File( dir, relativeLafClassName.substring( 0, relativeLafClassName.lastIndexOf( '.' ) ).replace( '.', '/' ) )
				: dir;

			dump( lafClassName, dir2 );
		}
	}

	private static void dump( String lookAndFeelClassName, File dir ) {
		try {
			UIManager.setLookAndFeel( lookAndFeelClassName );
		} catch( Exception ex ) {
			ex.printStackTrace();
			return;
		}

		dump( dir, null );
	}

	private static void dump( File dir, String name ) {
		LookAndFeel lookAndFeel = UIManager.getLookAndFeel();

		dump( dir, name, "", lookAndFeel, key -> !key.contains( "InputMap" ) );
		if( lookAndFeel.getClass() != FlatLightLaf.class )
			return;

		dump( dir, name, "_InputMap", lookAndFeel, key -> key.contains( "InputMap" ) );
	}

	private static void dump( File dir, String name, String nameSuffix,
		LookAndFeel lookAndFeel, Predicate<String> keyFilter )
	{
		// dump to string
		StringWriter stringWriter = new StringWriter( 100000 );
		new UIDefaultsDump( lookAndFeel ).dump( new PrintWriter( stringWriter ), keyFilter );

		if( name == null ) {
			name = lookAndFeel instanceof MyBasicLookAndFeel
				? BasicLookAndFeel.class.getSimpleName()
				: lookAndFeel.getClass().getSimpleName();
		}
		String osSuffix = (SystemInfo.IS_MAC && lookAndFeel instanceof FlatLaf) ? "-mac" : "";
		File file = new File( dir, name + nameSuffix + "_"
			+ System.getProperty( "java.version" ) + osSuffix + ".txt" );

		// write to file
		file.getParentFile().mkdirs();
		try( FileWriter fileWriter = new FileWriter( file ) ) {
			fileWriter.write( stringWriter.toString().replace( "\r", "" ) );
		} catch( IOException ex ) {
			ex.printStackTrace();
		}
	}

	private UIDefaultsDump( LookAndFeel lookAndFeel ) {
		this.lookAndFeel = lookAndFeel;
		this.defaults = lookAndFeel.getDefaults();
	}

	private void dump( PrintWriter out, Predicate<String> keyFilter ) {
		Class<?> lookAndFeelClass = lookAndFeel instanceof MyBasicLookAndFeel
			? BasicLookAndFeel.class
			: lookAndFeel.getClass();
		out.printf( "Class  %s%n", lookAndFeelClass.getName() );
		out.printf( "ID     %s%n", lookAndFeel.getID() );
		out.printf( "Name   %s%n", lookAndFeel.getName() );
		out.printf( "Java   %s%n", System.getProperty( "java.version" ) );
		out.printf( "OS     %s%n", System.getProperty( "os.name" ) );

		defaults.entrySet().stream()
			.sorted( (key1, key2) -> {
				return String.valueOf( key1 ).compareTo( String.valueOf( key2 ) );
			} )
			.forEach( entry -> {
				Object key = entry.getKey();
				Object value = entry.getValue();

				String strKey = String.valueOf( key );
				if( !keyFilter.test( strKey ) )
					return;

				int dotIndex = strKey.indexOf( '.' );
				String prefix = (dotIndex > 0)
					? strKey.substring( 0, dotIndex )
					: strKey.endsWith( "UI" )
						? strKey.substring( 0, strKey.length() - 2 )
						: "";
				if( !prefix.equals( lastPrefix ) ) {
					lastPrefix = prefix;
					out.printf( "%n%n#---- %s ----%n%n", prefix );
				}

				out.printf( "%-30s ", strKey );
				dumpValue( out, value );
				out.println();
			} );
	}

	private void dumpValue( PrintWriter out, Object value ) {
		if( value == null ||
			value instanceof String ||
			value instanceof Number ||
			value instanceof Boolean )
		{
			out.print( value );
		} else if( value instanceof Character ) {
			char ch = ((Character)value).charValue();
			if( ch >= ' ' && ch <= '~' )
				out.printf( "'%c'", value );
			else
				out.printf( "'\\u%h'", (int) ch );
		} else if( value.getClass().isArray() )
			dumpArray( out, value );
		else if( value instanceof List )
			dumpList( out, (List<?>) value );
		else if( value instanceof Color )
			dumpColor( out, (Color) value );
		else if( value instanceof Font )
			dumpFont( out, (Font) value );
		else if( value instanceof Insets )
			dumpInsets( out, (Insets) value );
		else if( value instanceof Dimension )
			dumpDimension( out, (Dimension) value );
		else if( value instanceof Border )
			dumpBorder( out, (Border) value, null );
		else if( value instanceof Icon )
			dumpIcon( out, (Icon) value );
		else if( value instanceof ListCellRenderer )
			dumpListCellRenderer( out, (ListCellRenderer<?>) value );
		else if( value instanceof InputMap )
			dumpInputMap( out, (InputMap) value, null );
		else if( value instanceof LazyValue )
			dumpLazyValue( out, (LazyValue) value );
		else if( value instanceof ActiveValue )
			dumpActiveValue( out, (ActiveValue) value );
		else
			out.printf( "[unknown type] %s", dumpClass( value ) );
	}

	private void dumpArray( PrintWriter out, Object array ) {
		int length = Array.getLength( array );
		out.printf( "length=%d    %s", length, dumpClass( array ) );
		for( int i = 0; i < length; i++ ) {
			out.printf( "%n    [%d] ", i );
			dumpValue( out, Array.get( array, i ) );
		}
	}

	private void dumpList( PrintWriter out, List<?> list ) {
		out.printf( "size=%d    %s", list.size(), dumpClass( list ) );
		for( int i = 0; i < list.size(); i++ ) {
			out.printf( "%n    [%d] ", i );
			dumpValue( out, list.get( i ) );
		}
	}

	private void dumpColor( PrintWriter out, Color color ) {
		boolean hasAlpha = (color.getAlpha() != 255);
		out.printf( hasAlpha ? "#%08x    %s" : "#%06x    %s",
			hasAlpha ? color.getRGB() : (color.getRGB() & 0xffffff),
			dumpClass( color ) );
	}

	private void dumpFont( PrintWriter out, Font font ) {
		String strStyle = font.isBold()
			? font.isItalic() ? "bolditalic" : "bold"
			: font.isItalic() ? "italic" : "plain";
		out.printf( "%s %s %d    %s",
			font.getName(), strStyle, font.getSize(),
			dumpClass( font ) );
	}

	private void dumpInsets( PrintWriter out, Insets insets ) {
		out.printf( "%d,%d,%d,%d    %s",
			insets.top, insets.left, insets.bottom, insets.right,
			dumpClass( insets ) );
	}

	private void dumpDimension( PrintWriter out, Dimension dimension ) {
		out.printf( "%d,%d    %s",
			dimension.width, dimension.height,
			dumpClass( dimension ) );
	}

	private void dumpBorder( PrintWriter out, Border border, String indent ) {
		if( indent == null )
			indent = "";
		out.print( indent );

		if( border == null ) {
			out.print( "null" );
			return;
		}

		if( border instanceof CompoundBorder ) {
			CompoundBorder b = (CompoundBorder) border;
			out.println( dumpClass( b ) );
			dumpBorder( out, b.getOutsideBorder(), indent + "    " );
			out.println();
			dumpBorder( out, b.getInsideBorder(), indent + "    " );
		} else {
			if( border instanceof LineBorder ) {
				LineBorder b = (LineBorder) border;
				out.print( "line: " );
				dumpValue( out, b.getLineColor() );
				out.printf( " %d %b    ", b.getThickness(), b.getRoundedCorners() );
			}

			if( dummyComponent == null )
				dummyComponent = new JComponent() {};

			JComponent c = dummyComponent;

			String borderClassName = border.getClass().getName();
			if( border instanceof BorderUIResource ) {
				try {
					Field f = BorderUIResource.class.getDeclaredField( "delegate" );
					f.setAccessible( true );
					Object delegate = f.get( border );
					borderClassName = delegate.getClass().getName();
				} catch( Exception ex ) {
					ex.printStackTrace();
				}
			}

			switch( borderClassName ) {
				case "com.apple.laf.AquaToolBarUI$ToolBarBorder":
				case "org.pushingpixels.substance.internal.utils.border.SubstanceToolBarBorder":
					c = new JToolBar();
					break;

				case "com.jgoodies.looks.plastic.PlasticBorders$InternalFrameBorder":
					c = new JInternalFrame();
					break;
			}

			Insets insets = border.getBorderInsets( c );
			out.printf( "%d,%d,%d,%d  %b    %s",
				insets.top, insets.left, insets.bottom, insets.right,
				border.isBorderOpaque(),
				dumpClass( border ) );
		}
	}

	private void dumpIcon( PrintWriter out, Icon icon ) {
		out.printf( "%d,%d    %s",
			icon.getIconWidth(), icon.getIconHeight(),
			dumpClass( icon ) );
		if( icon instanceof ImageIcon )
			out.printf( "  (%s)", dumpClass( ((ImageIcon)icon).getImage() ) );
	}

	private void dumpListCellRenderer( PrintWriter out, ListCellRenderer<?> listCellRenderer ) {
		out.print( dumpClass( listCellRenderer ) );
	}

	private void dumpInputMap( PrintWriter out, InputMap inputMap, String indent ) {
		if( indent == null )
			indent = "    ";

		out.printf( "%d    %s", inputMap.size(), dumpClass( inputMap ) );

		KeyStroke[] keys = inputMap.keys();
		if( keys != null ) {
			Arrays.sort( keys, (keyStroke1, keyStroke2) -> {
				return String.valueOf( keyStroke1 ).compareTo( String.valueOf( keyStroke2 ) );
			} );
			for( KeyStroke keyStroke : keys ) {
				Object value = inputMap.get( keyStroke );
				String strKeyStroke = keyStroke.toString().replace( "pressed ", "" );
				out.printf( "%n%s%-20s  %s", indent, strKeyStroke, value );
			}
		}

		InputMap parent = inputMap.getParent();
		if( parent != null )
			dumpInputMap( out, parent, indent + "    " );
	}

	private void dumpLazyValue( PrintWriter out, LazyValue value ) {
		out.print( "[lazy] " );
		dumpValue( out, value.createValue( defaults ) );
	}

	private void dumpActiveValue( PrintWriter out, ActiveValue value ) {
		out.print( "[active] " );
		Object realValue = value.createValue( defaults );

		if( realValue instanceof Font && realValue == UIManager.getFont( "defaultFont" ) ) {
			// dump "$defaultFont" for the default font to make it easier to
			// compare Windows/Linux dumps with macOS dumps
			out.print( "$defaultFont" );
			if( realValue instanceof UIResource )
				out.print( " [UI]" );
		} else
			dumpValue( out, realValue );
	}

	private String dumpClass( Object value ) {
		String classname = value.getClass().getName();
		if( value instanceof UIResource )
			classname += " [UI]";
		return classname;
	}

	//---- class MyBasicLookAndFeel -------------------------------------------

	public static class MyBasicLookAndFeel
		extends BasicLookAndFeel
	{
		@Override public String getName() { return "Basic"; }
		@Override public String getID() { return "Basic"; }
		@Override public String getDescription() { return "Basic"; }
		@Override public boolean isNativeLookAndFeel() { return false; }
		@Override public boolean isSupportedLookAndFeel() { return true; }
	}
}
