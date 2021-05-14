/*
 *     Copyright 2021 Horstexplorer @ https://www.netbeacon.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.netbeacon.xenia.bot.utils.level;

import de.netbeacon.xenia.backend.client.objects.external.Member;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Objects;
import java.util.Random;

public class LevelPointCard{

	private static Font FONT;

	private static final int CARD_BORDER = 20;
	private static final int CARD_WIDTH = 1440;
	private static final int CARD_HEIGHT = 360;
	private static final float FONT_SIZE = 60f;

	public enum ColorSet {
		Primary,
		Secondary,
		Tertiary
	}

	static final Color[] COLORS = new Color[]{
		new Color(175,255, 20),
		new Color(255, 20, 175),
		new Color(20, 175, 225),
		new Color(255, 127, 0)
	};

	static final Random random = new Random();

	private ByteArrayOutputStream baos;

	static {
		try{
			FONT = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(LevelPointCard.class.getClassLoader().getResourceAsStream("fonts/ethnocentricrg.ttf")));
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public LevelPointCard(Member member){
		String name = member.metaNickname();
		long levelI = LevelPointManager.calculateLevel(member);
		long min = LevelPointManager.calculateLevelMin(levelI);
		long max = LevelPointManager.calculateLevelMax(levelI);
		long cur = member.getLevelPoints();
		String level = "lvl "+levelI;
		try{
			BufferedImage card = new BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB);
			Graphics2D cardGraphics = card.createGraphics();
			// prepare background
			cardGraphics.setColor(Color.BLACK);
			cardGraphics.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT);
			// draw random background
			drawBackground(cardGraphics, CARD_WIDTH, CARD_HEIGHT);
			// draw name
			drawText(cardGraphics, FONT, Font.BOLD, calculateFontSize(FONT_SIZE, 25, name), new Color(210, 210, 210), name, CARD_BORDER, (int) FONT_SIZE + CARD_BORDER, Position.Left);
			// draw ground
			cardGraphics.setColor(new Color(20, 20, 20));
			cardGraphics.fillRoundRect(420, CARD_HEIGHT - 4 * CARD_BORDER,CARD_WIDTH - 420 - 2 * CARD_BORDER, 3 * CARD_BORDER, CARD_BORDER * 4, CARD_BORDER * 4);
			// draw bar
			int colorId = random.nextInt(COLORS.length);
			drawProgressBar(cardGraphics, COLORS[colorId], min, cur, max, 420, CARD_HEIGHT - 4 * CARD_BORDER, CARD_WIDTH - 420 - 2 * CARD_BORDER, 3 * CARD_BORDER, CARD_BORDER * 4);
			drawProgressBar(cardGraphics, COLORS[colorId].darker(), min, cur, max, 420, CARD_HEIGHT - 4 * CARD_BORDER, CARD_WIDTH - 420 - 5 * CARD_BORDER, 3 * CARD_BORDER, CARD_BORDER * 4);
			drawProgressBar(cardGraphics, COLORS[colorId].darker().darker(), min, cur, max, 420, CARD_HEIGHT - 4 * CARD_BORDER, CARD_WIDTH - 420 - 15 * CARD_BORDER, 3 * CARD_BORDER, CARD_BORDER * 4);
			// draw text on bar
			drawText(cardGraphics, FONT, Font.BOLD, calculateFontSize(FONT_SIZE / 3, 8, String.valueOf(min)), Color.GRAY, String.valueOf(min), 420, CARD_HEIGHT - 3 * CARD_BORDER - (int) FONT_SIZE / 2, Position.Left);
			drawText(cardGraphics, FONT, Font.BOLD, calculateFontSize(FONT_SIZE / 2, 8, String.valueOf(cur)), Color.WHITE, String.valueOf(cur), CARD_WIDTH - 2 * CARD_BORDER - 490, CARD_HEIGHT - CARD_BORDER - (int) FONT_SIZE / 3, Position.Center);
			drawText(cardGraphics, FONT, Font.BOLD, calculateFontSize(FONT_SIZE / 2.5F, 8, level), Color.WHITE, level, CARD_WIDTH - 2 * CARD_BORDER - 490, CARD_HEIGHT - CARD_BORDER - 7 * (int)FONT_SIZE / 6, Position.Center);
			drawText(cardGraphics, FONT, Font.BOLD, calculateFontSize(FONT_SIZE / 3, 8, String.valueOf(max)), Color.GRAY, String.valueOf(max), CARD_WIDTH - 2 * CARD_BORDER, CARD_HEIGHT - 3 * CARD_BORDER - (int) FONT_SIZE / 2, Position.Right);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(card, "png", baos);
			this.baos = baos;
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public ByteArrayOutputStream getByteArrayOutputStream(){
		return baos;
	}

	private void drawBackground(Graphics2D graphics2D, int x2, int y2){
		for(int i = 0; i < random.nextInt(50) + 1; i++){
			boolean p = random.nextBoolean();
			boolean q = random.nextBoolean();

			int color = random.nextInt(COLORS.length);

			int xa = random.nextInt(x2);
			int xb = random.nextInt(x2);
			int ya = random.nextInt(y2);
			int yb = random.nextInt(y2);
			graphics2D.setColor(COLORS[color].darker());
			for(int ii = 0; ii < 2; ii++){
				graphics2D.setColor(graphics2D.getColor().darker());
				int width = 10;
				for(int iii = 0; iii < width; iii++){
					graphics2D.drawLine(
						p ? 0 : xa+iii+width*ii,
						p ? ya+iii+width+width*ii : 0,
						q ? x2 : xb-iii+width*ii,
						q ? yb-iii+width*ii : y2
					);
				}
			}
		}
	}

	private void drawProgressBar(Graphics2D graphics2D, Color bar, long min, long current, long max, int x, int y, int width, int height, int rad) {
		int cwdth = (int) ((width / (float) (max - min)) * (float)(current - min));
		graphics2D.setColor(bar);
		graphics2D.fillRect(x + rad/2, y + 0 * height / 8, cwdth - cwdth / 3 - rad/2, height / 8 + 1);
		graphics2D.fillRect(x + rad/5, y + 1 * height / 8, cwdth - cwdth / 7 - rad/5, height / 8 + 1);
		graphics2D.fillRect(x + rad/10, y + 2 * height / 8, cwdth - cwdth / 15 - rad/10, height / 8 + 1);
		graphics2D.fillRect(x + rad/30, y + 3 * height / 8, cwdth - cwdth / 5 - rad/30, height / 8 + 1);
		graphics2D.fillRect(x + rad/30, y + 4 * height / 8, cwdth - cwdth / 35 - rad/30, height / 8 + 1);
		graphics2D.fillRect(x + rad/10, y + 5 * height / 8, cwdth - 0 - rad/10, height / 8 + 1);
		graphics2D.fillRect(x + rad/5, y + 6 * height / 8, cwdth - cwdth / 5 - rad/5, height / 8 + 1);
		graphics2D.fillRect(x + rad/2, y + 7 * height / 8, cwdth - cwdth / 2 - rad/2, height / 8);
	}

	public enum Position{
		Left,
		Center,
		Right
	}

	private float calculateFontSize(float maxSize, int unscaledLimit, String text){
		return text.length() > unscaledLimit ? maxSize * unscaledLimit / (float) text.length() : maxSize;
	}

	private void drawText(Graphics2D graphics2D, Font font, int fontType, float fontSize, Color fontColor, String text, int x, int y, Position position){
		graphics2D.setFont(font.deriveFont(fontSize).deriveFont(fontType));
		graphics2D.setColor(fontColor);
		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		int xs = x;
		if(position.equals(Position.Center)){
			xs = x - (int) (text.length() * fontSize) / 2;
		}else if(position.equals(Position.Right)){
			xs = x - (int) (text.length() * fontSize);
		}
		graphics2D.drawString(text, xs, y);
	}
}
