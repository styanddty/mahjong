package jp.sourceforge.andjong;

import static jp.sourceforge.andjong.Hai.OLD_KIND_FON;
import static jp.sourceforge.andjong.Hai.OLD_KIND_MASK;
import static jp.sourceforge.andjong.Hai.OLD_KIND_SANGEN;
import static jp.sourceforge.andjong.Hai.OLD_KIND_TON;
import static jp.sourceforge.andjong.Hai.OLD_KIND_TSUU;
import jp.sourceforge.andjong.Tehai.Combi;
import jp.sourceforge.andjong.Tehai.CountFormat;

public class AgariScore {
	/** カウントフォーマット */
	private CountFormat countFormat = new CountFormat();
	
	/**
	 * 符を計算します。
	 * 
	 * @param tehai
	 *            手牌 addHai 和了った牌 combi 手牌の組み合わせ　
	 * 
	 * @return int 符
	 * 
	 */
	private int countHu(Tehai tehai, Hai addHai, Combi combi, Yaku yaku,AgariSetting setting) {
		int countHu = 20;
		int id;
		Hai checkHai[][];
		
		//七対子の場合は２５符
		if(yaku.checkTeetoitu() == true){
			return 25;
		}

		id = combi.atamaId;

		// ３元牌なら２符追加
		if ((id & OLD_KIND_SANGEN) != 0) {
			countHu += 2;
		}

		// 場風なら２符追加
		if (id == OLD_KIND_TON) {
			countHu += 2;
		}

		// 自風なら２符追加
		if (((id & OLD_KIND_FON) != 0)
				&& (id & OLD_KIND_MASK) == (setting.getJikaze())) {
			countHu += 2;
		}
		
		//平和が成立する場合は、待ちによる２符追加よりも優先される
		if(yaku.checkPinfu() == false){
			// 単騎待ちの場合２符追加
			if(id == combi.atamaId){
				countHu += 2;
			}
			
			// 嵌張待ちの場合２符追加
			id = addHai.getId();
			//数牌の２〜８かどうか判定
			if(((id & OLD_KIND_TSUU) == 0 )
					&& ( ((id & OLD_KIND_MASK) >=2) || ((id & OLD_KIND_MASK) <= 8))){
				for(int i = 0 ; i < combi.shunCount ; i++){
					if( (id-1) == combi.shunIds[i]){
						countHu += 2;
					}
				}
			}
	
			// 辺張待ち(3)の場合２符追加
			if(((id & OLD_KIND_TSUU) == 0 ) && ((id & OLD_KIND_MASK) ==3) ){
				for(int i = 0 ; i < combi.shunCount ; i++){
					if( (id-2) == combi.shunIds[i]){
						countHu += 2;
					}
				}
			}
	
			// 辺張待ち(7)の場合２符追加
			if(((id & OLD_KIND_TSUU) == 0 ) && ((id & OLD_KIND_MASK) ==7) ){
				for(int i = 0 ; i < combi.shunCount ; i++){
					if( id == combi.shunIds[i]){
						countHu += 2;
					}
				}
			}
		}

		// 暗刻による加点
		for (int i = 0; i < combi.kouCount; i++) {
			id = combi.kouIds[i];
			// 牌が字牌もしくは1,9
			if (((id & OLD_KIND_TSUU) != 0) || ((id & OLD_KIND_MASK) == 1)
					|| ((id & OLD_KIND_MASK) == 9)) {
				countHu += 8;
			} else {
				countHu += 4;
			}
		}

		// 明刻による加点
		for (int i = 0; i < tehai.getMinkousLength(); i++) {
			checkHai = tehai.getMinkous();
			id = checkHai[i][0].getOldId();
			// 牌が字牌もしくは1,9
			if (((id & OLD_KIND_TSUU) != 0) || ((id & OLD_KIND_MASK) == 1)
					|| ((id & OLD_KIND_MASK) == 9)) {
				countHu += 4;
			} else {
				countHu += 2;
			}
		}

		// 明槓による加点
		for (int i = 0; i < tehai.getMinkansLength(); i++) {
			checkHai = tehai.getMinkans();
			id = checkHai[i][0].getOldId();
			// 牌が字牌もしくは1,9
			if (((id & OLD_KIND_TSUU) != 0) || ((id & OLD_KIND_MASK) == 1)
					|| ((id & OLD_KIND_MASK) == 9)) {
				countHu += 16;
			} else {
				countHu += 8;
			}
		}

		// 暗槓による加点
		for (int i = 0; i < tehai.getAnkansLength(); i++) {
			checkHai = tehai.getAnkans();
			id = checkHai[i][0].getOldId();
			// 牌が字牌もしくは1,9
			if (((id & OLD_KIND_TSUU) != 0) || ((id & OLD_KIND_MASK) == 1)
					|| ((id & OLD_KIND_MASK) == 9)) {
				countHu += 32;
			} else {
				countHu += 16;
			}
		}

		// ツモ上がりで平和が成立していなければ２譜追加
		if(setting.getYakuflg(AgariSetting.YakuflgName.TUMO.ordinal() )== true){
			if(yaku.checkPinfu() == false){
				countHu += 2;
			}
		}
		
		// 面前ロン上がりの場合は１０符追加
		if(setting.getYakuflg(AgariSetting.YakuflgName.TUMO.ordinal() )== false){
			if (yaku.nakiflg == false) {
				countHu += 10;
			}
		}

		// 一の位がある場合は、切り上げ
		if (countHu % 10 != 0) {
			countHu = countHu - (countHu % 10) + 10;
		}

		return countHu;
	}

	/**
	 * 上がり点数を取得します。
	 * 
	 * @param tehai
	 *            手牌 addHai 和了った牌 combi 手牌の組み合わせ　
	 * 
	 * @return int 和了り点
	 */
	public int getScore(int hanSuu, int huSuu) {
		int score;
		// 符　× ２の　（翻数　+　場ゾロの2翻)乗
		score = huSuu * (int) Math.pow(2, hanSuu + 2);
		// 子は上の4倍が基本点(親は6倍)
		score *= 4;

		// 100で割り切れない数がある場合100点繰上げ
		if (score % 100 != 0) {
			score = score - (score % 100) + 100;
		}
		// 7700以上は8000とする
		if (score >= 7700) {
			score = 8000;
		}

		if (hanSuu >= 13) { // 13翻以上は役満
			score = 32000;
		} else if (hanSuu >= 11) { // 11翻以上は3倍満
			score = 24000;
		} else if (hanSuu >= 8) { // 8翻以上は倍満
			score = 16000;
		} else if (hanSuu >= 6) { // 6翻以上は跳満
			score = 12000;
		} else if (hanSuu >= 5) { // 5翻以上は満貫
			score = 8000;
		}

		return score;
	}

	public int getAgariScore(Tehai tehai, Hai addHai, Combi[] combis,AgariSetting setting) {
		// カウントフォーマットを取得します。
		tehai.getCountFormat(countFormat, addHai);

		// あがりの組み合わせを取得します。
		int combisCount = tehai.getCombi(combis, countFormat);

		// あがりの組み合わせがない場合は0点
		if (combisCount == 0)
			return 0;

		// 役
		int hanSuu[] = new int[combisCount];
		// 符
		int huSuu[] = new int[combisCount];
		// 点数（子のロン上がり）
		int agariScore[] = new int[combisCount];
		// 最大の点数
		int maxagariScore = 0;

		for (int i = 0; i < combisCount; i++) {
			Yaku yaku = new Yaku(tehai, addHai, combis[i], setting);
			hanSuu[i] = yaku.getHanSuu();
			huSuu[i] = countHu(tehai, addHai, combis[i],yaku,setting);
			agariScore[i] = getScore(hanSuu[i], huSuu[i]);
		}

		// 最大値を探す
		maxagariScore = agariScore[0];
		for (int i = 0; i < combisCount; i++) {
			maxagariScore = Math.max(maxagariScore, agariScore[i]);
		}
		return maxagariScore;
	}
}
