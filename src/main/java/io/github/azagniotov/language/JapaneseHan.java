package io.github.azagniotov.language;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The Jōyō Kanji List.
 *
 * <p>The Jōyō kanji (常用漢字, "regular-use kanji") are those kanji listed on the Jōyō kanji hyō
 * (常用漢字表, "list of regular-use kanji"), officially announced by the Japanese Ministry of Education.
 *
 * <p>The current list of 2,136 characters was issued in 2010. It is a slightly modified version of
 * the tōyō kanji, which was the initial list of secondary school-level kanji standardized after
 * World War II.
 *
 * <p>The list is not a comprehensive list of all characters and readings in regular use; rather, it
 * is intended as a literacy baseline for those who have completed compulsory education, as well as
 * a list of permitted characters and readings for use in official government documents.
 *
 * <p>Due to the requirement that official government documents make use of only jōyō kanji and
 * their readings, several rare characters are also included due to their use in the Constitution of
 * Japan, which was being written at the same time the original 1,850-character tōyō kanji list was
 * compiled.
 */
class JapaneseHan {

  private static final Set<Character> JOUYOU_SET =
      new HashSet<>(
          Arrays.asList(
              '一', '乙', '九', '七', '十', '人', '丁', '刀', '二', '入', '八', '又', '了', '力', '下', '干', '丸',
              '久', '及', '弓', '巾', '己', '口', '工', '乞', '才', '三', '山', '士', '子', '女', '小', '上', '丈',
              '刃', '寸', '夕', '千', '川', '大', '土', '亡', '凡', '万', '与', '引', '円', '王', '化', '火', '牙',
              '介', '刈', '牛', '凶', '斤', '区', '欠', '月', '犬', '元', '幻', '戸', '五', '互', '午', '公', '勾',
              '孔', '今', '支', '止', '氏', '尺', '手', '収', '升', '少', '冗', '心', '仁', '水', '井', '切', '双',
              '太', '丹', '中', '弔', '爪', '天', '斗', '屯', '内', '匂', '日', '反', '比', '匹', '不', '夫', '父',
              '仏', '分', '文', '片', '方', '乏', '木', '毛', '厄', '友', '予', '六', '圧', '以', '右', '永', '凹',
              '央', '加', '可', '瓦', '外', '且', '刊', '甘', '丘', '旧', '去', '巨', '玉', '句', '兄', '穴', '玄',
              '古', '功', '巧', '広', '甲', '号', '込', '左', '冊', '札', '皿', '仕', '史', '司', '四', '市', '矢',
              '示', '叱', '失', '写', '主', '囚', '汁', '出', '処', '召', '尻', '申', '世', '正', '生', '斥', '石',
              '仙', '占', '他', '打', '代', '台', '旦', '庁', '田', '奴', '冬', '凸', '丼', '尼', '白', '半', '氾',
              '犯', '皮', '必', '氷', '付', '布', '払', '丙', '平', '辺', '弁', '母', '包', '北', '本', '末', '未',
              '民', '矛', '目', '由', '幼', '用', '立', '令', '礼', '扱', '安', '衣', '芋', '印', '因', '宇', '羽',
              '汚', '仮', '回', '灰', '会', '各', '汗', '缶', '企', '伎', '危', '机', '気', '吉', '休', '吸', '朽',
              '臼', '共', '叫', '仰', '曲', '刑', '血', '件', '交', '光', '向', '后', '好', '江', '考', '行', '合',
              '再', '在', '旨', '死', '糸', '至', '字', '寺', '次', '耳', '自', '式', '芝', '守', '朱', '州', '舟',
              '充', '旬', '巡', '如', '匠', '色', '尽', '迅', '成', '西', '舌', '先', '全', '壮', '早', '争', '存',
              '多', '宅', '団', '地', '池', '竹', '仲', '虫', '兆', '伝', '吐', '灯', '当', '同', '弐', '肉', '任',
              '年', '肌', '伐', '帆', '汎', '妃', '百', '伏', '米', '忙', '朴', '毎', '名', '妄', '有', '羊', '吏',
              '両', '列', '劣', '老', '亜', '位', '囲', '医', '壱', '応', '何', '花', '我', '快', '戒', '改', '貝',
              '角', '完', '肝', '含', '岐', '希', '忌', '汽', '技', '却', '求', '究', '狂', '局', '均', '近', '吟',
              '串', '君', '形', '系', '芸', '迎', '決', '見', '言', '呉', '坑', '孝', '抗', '攻', '更', '克', '告',
              '谷', '困', '佐', '沙', '災', '材', '作', '伺', '志', '私', '似', '児', '社', '車', '寿', '秀', '住',
              '初', '助', '序', '床', '抄', '肖', '条', '状', '伸', '臣', '芯', '身', '辛', '図', '吹', '杉', '声',
              '赤', '折', '走', '即', '束', '足', '村', '汰', '妥', '対', '体', '択', '沢', '但', '男', '沖', '町',
              '沈', '低', '呈', '廷', '弟', '努', '投', '豆', '那', '尿', '妊', '忍', '把', '売', '伯', '麦', '抜',
              '伴', '判', '坂', '阪', '否', '批', '尾', '肘', '扶', '兵', '別', '返', '芳', '邦', '坊', '妨', '忘',
              '防', '没', '妙', '冶', '役', '余', '妖', '抑', '沃', '来', '乱', '卵', '利', '里', '良', '冷', '励',
              '戻', '呂', '労', '弄', '宛', '依', '委', '育', '雨', '泳', '英', '易', '延', '沿', '炎', '往', '押',
              '旺', '欧', '殴', '岡', '佳', '価', '果', '河', '苛', '画', '芽', '怪', '拐', '劾', '拡', '学', '岳',
              '官', '岸', '岩', '玩', '奇', '祈', '季', '宜', '泣', '居', '拒', '拠', '京', '享', '供', '協', '況',
              '金', '苦', '具', '空', '屈', '径', '茎', '券', '肩', '弦', '呼', '固', '股', '虎', '効', '幸', '拘',
              '肯', '刻', '国', '昆', '妻', '采', '刷', '刹', '参', '使', '刺', '始', '姉', '枝', '祉', '肢', '事',
              '侍', '治', '実', '舎', '者', '邪', '若', '取', '受', '呪', '周', '宗', '叔', '述', '所', '尚', '招',
              '承', '昇', '松', '沼', '垂', '炊', '枢', '制', '姓', '征', '性', '青', '斉', '昔', '析', '拙', '狙',
              '阻', '卒', '卓', '拓', '担', '知', '宙', '忠', '抽', '注', '長', '直', '坪', '定', '底', '抵', '邸',
              '泥', '的', '迭', '典', '店', '妬', '東', '到', '毒', '突', '届', '奈', '乳', '念', '波', '拝', '杯',
              '拍', '泊', '迫', '板', '版', '彼', '披', '肥', '非', '泌', '表', '苗', '府', '怖', '阜', '附', '侮',
              '武', '服', '沸', '物', '併', '並', '歩', '奉', '宝', '抱', '放', '法', '泡', '房', '肪', '牧', '奔',
              '妹', '枚', '枕', '抹', '味', '岬', '命', '明', '免', '茂', '盲', '門', '夜', '弥', '油', '拉', '林',
              '例', '炉', '和', '枠', '哀', '威', '為', '畏', '胃', '茨', '咽', '姻', '映', '栄', '疫', '怨', '屋',
              '卸', '音', '科', '架', '悔', '海', '界', '皆', '垣', '柿', '革', '括', '活', '冠', '巻', '看', '紀',
              '軌', '客', '逆', '虐', '急', '級', '糾', '峡', '挟', '狭', '軍', '係', '型', '契', '計', '建', '研',
              '県', '限', '孤', '弧', '故', '枯', '後', '侯', '厚', '恒', '洪', '皇', '紅', '荒', '郊', '香', '拷',
              '恨', '査', '砂', '砕', '削', '昨', '柵', '咲', '拶', '姿', '思', '指', '施', '持', '室', '狩', '首',
              '拾', '秋', '臭', '柔', '重', '祝', '俊', '春', '盾', '叙', '昭', '乗', '城', '浄', '拭', '食', '侵',
              '信', '津', '神', '甚', '帥', '是', '政', '星', '牲', '省', '窃', '宣', '専', '泉', '浅', '洗', '染',
              '前', '祖', '奏', '相', '荘', '草', '送', '促', '則', '俗', '耐', '待', '怠', '胎', '退', '単', '炭',
              '胆', '段', '茶', '昼', '柱', '挑', '勅', '珍', '追', '亭', '貞', '帝', '訂', '点', '度', '怒', '逃',
              '洞', '峠', '独', '栃', '南', '虹', '派', '背', '肺', '畑', '発', '卑', '飛', '眉', '美', '秒', '品',
              '訃', '負', '赴', '封', '風', '柄', '変', '便', '保', '胞', '某', '冒', '勃', '盆', '昧', '迷', '面',
              '約', '勇', '幽', '洋', '要', '律', '柳', '侶', '厘', '郎', '挨', '案', '員', '院', '唄', '畝', '浦',
              '益', '悦', '宴', '桜', '翁', '俺', '恩', '夏', '家', '荷', '華', '蚊', '害', '格', '核', '株', '釜',
              '陥', '既', '記', '起', '飢', '鬼', '帰', '宮', '挙', '恐', '恭', '胸', '脅', '訓', '郡', '恵', '桁',
              '倹', '兼', '剣', '拳', '軒', '原', '個', '庫', '娯', '悟', '候', '校', '耕', '航', '貢', '降', '高',
              '剛', '骨', '根', '唆', '差', '座', '挫', '宰', '栽', '剤', '財', '索', '殺', '桟', '蚕', '残', '師',
              '恣', '紙', '脂', '時', '疾', '射', '借', '酌', '弱', '殊', '珠', '酒', '修', '袖', '従', '准', '殉',
              '純', '書', '徐', '除', '宵', '将', '消', '症', '祥', '称', '笑', '辱', '唇', '娠', '振', '浸', '真',
              '針', '陣', '粋', '衰', '凄', '逝', '席', '脊', '隻', '扇', '栓', '租', '素', '倉', '捜', '挿', '桑',
              '造', '息', '捉', '速', '孫', '帯', '泰', '託', '値', '恥', '致', '畜', '逐', '秩', '衷', '酎', '捗',
              '朕', '通', '庭', '逓', '哲', '展', '徒', '途', '倒', '凍', '唐', '島', '桃', '討', '透', '党', '胴',
              '匿', '特', '悩', '納', '能', '破', '馬', '俳', '配', '倍', '梅', '剝', '班', '畔', '般', '疲', '秘',
              '被', '姫', '俵', '病', '浜', '敏', '浮', '粉', '紛', '陛', '勉', '哺', '捕', '俸', '倣', '峰', '砲',
              '剖', '紡', '埋', '脈', '眠', '娘', '冥', '耗', '紋', '容', '浴', '流', '留', '竜', '旅', '料', '倫',
              '涙', '烈', '恋', '連', '朗', '浪', '脇', '悪', '尉', '異', '移', '萎', '域', '逸', '淫', '陰', '液',
              '菓', '貨', '械', '崖', '涯', '殻', '郭', '掛', '喝', '渇', '乾', '勘', '患', '貫', '眼', '基', '寄',
              '規', '亀', '偽', '菊', '脚', '救', '球', '虚', '許', '魚', '強', '教', '郷', '菌', '惧', '偶', '掘',
              '啓', '掲', '渓', '経', '蛍', '健', '険', '現', '舷', '康', '控', '梗', '黄', '黒', '頃', '婚', '混',
              '痕', '紺', '彩', '採', '済', '祭', '斎', '細', '菜', '埼', '崎', '惨', '産', '斬', '視', '鹿', '執',
              '捨', '赦', '斜', '蛇', '釈', '寂', '授', '終', '羞', '習', '週', '渋', '宿', '淑', '粛', '術', '庶',
              '唱', '商', '渉', '章', '紹', '訟', '剰', '常', '情', '深', '紳', '進', '推', '酔', '崇', '据', '清',
              '盛', '惜', '戚', '責', '接', '設', '雪', '旋', '船', '措', '粗', '組', '巣', '掃', '曹', '曽', '爽',
              '窓', '側', '族', '率', '唾', '堆', '袋', '逮', '第', '脱', '探', '淡', '断', '窒', '著', '帳', '張',
              '彫', '眺', '釣', '頂', '鳥', '陳', '停', '偵', '笛', '添', '転', '都', '悼', '盗', '陶', '動', '堂',
              '得', '豚', '貪', '梨', '軟', '捻', '粘', '脳', '婆', '排', '敗', '培', '陪', '舶', '販', '票', '描',
              '猫', '貧', '瓶', '婦', '符', '部', '副', '閉', '偏', '崩', '訪', '望', '堀', '麻', '密', '務', '猛',
              '問', '野', '訳', '唯', '悠', '郵', '庸', '欲', '翌', '理', '陸', '略', '粒', '隆', '涼', '猟', '陵',
              '累', '握', '嵐', '偉', '椅', '飲', '運', '雲', '営', '詠', '越', '媛', '援', '奥', '温', '渦', '過',
              '賀', '絵', '開', '階', '街', '覚', '割', '葛', '寒', '喚', '堪', '換', '敢', '棺', '款', '間', '閑',
              '喜', '幾', '揮', '期', '棋', '貴', '欺', '喫', '給', '距', '御', '暁', '極', '勤', '琴', '筋', '遇',
              '隅', '敬', '景', '軽', '結', '圏', '堅', '検', '減', '湖', '雇', '喉', '慌', '港', '硬', '絞', '項',
              '詐', '最', '裁', '策', '酢', '傘', '散', '紫', '詞', '歯', '滋', '軸', '湿', '煮', '就', '衆', '集',
              '循', '順', '暑', '勝', '掌', '晶', '焼', '焦', '硝', '粧', '詔', '証', '象', '場', '畳', '植', '殖',
              '森', '診', '尋', '須', '遂', '随', '婿', '晴', '税', '絶', '善', '然', '疎', '訴', '創', '喪', '痩',
              '葬', '装', '測', '属', '尊', '堕', '惰', '替', '貸', '隊', '達', '棚', '短', '弾', '遅', '着', '貯',
              '朝', '貼', '超', '椎', '痛', '塚', '堤', '提', '程', '渡', '塔', '搭', '棟', '湯', '痘', '登', '答',
              '等', '筒', '統', '童', '道', '鈍', '廃', '媒', '買', '博', '斑', '飯', '晩', '番', '蛮', '悲', '扉',
              '費', '備', '筆', '評', '富', '普', '幅', '復', '雰', '塀', '遍', '補', '募', '報', '傍', '帽', '棒',
              '貿', '満', '無', '喩', '愉', '湧', '猶', '裕', '遊', '雄', '揚', '揺', '葉', '陽', '絡', '落', '痢',
              '硫', '量', '塁', '裂', '廊', '惑', '湾', '腕', '愛', '暗', '彙', '意', '違', '園', '煙', '猿', '遠',
              '鉛', '塩', '虞', '嫁', '暇', '禍', '靴', '雅', '塊', '楷', '解', '慨', '蓋', '該', '較', '隔', '楽',
              '滑', '褐', '勧', '寛', '幹', '感', '漢', '頑', '棄', '毀', '義', '詰', '嗅', '業', '僅', '禁', '愚',
              '窟', '群', '傾', '携', '継', '詣', '隙', '傑', '嫌', '献', '絹', '遣', '源', '誇', '鼓', '碁', '溝',
              '鉱', '傲', '債', '催', '塞', '歳', '載', '罪', '搾', '嗣', '試', '詩', '資', '飼', '慈', '辞', '嫉',
              '腫', '愁', '酬', '準', '署', '傷', '奨', '照', '詳', '蒸', '飾', '触', '寝', '慎', '新', '腎', '睡',
              '数', '裾', '勢', '聖', '誠', '跡', '摂', '節', '戦', '煎', '羨', '腺', '詮', '践', '禅', '塑', '僧',
              '想', '賊', '続', '損', '滞', '滝', '嘆', '暖', '痴', '稚', '置', '蓄', '腸', '跳', '賃', '艇', '溺',
              '鉄', '塡', '殿', '電', '塗', '働', '督', '頓', '農', '漠', '鉢', '搬', '煩', '頒', '微', '福', '腹',
              '墓', '蜂', '豊', '飽', '睦', '幕', '夢', '盟', '滅', '誉', '預', '溶', '腰', '裸', '雷', '酪', '裏',
              '慄', '虜', '鈴', '零', '廉', '賂', '路', '楼', '話', '賄', '維', '隠', '駅', '演', '寡', '歌', '箇',
              '概', '閣', '慣', '管', '関', '旗', '疑', '漁', '境', '銀', '駆', '熊', '語', '誤', '構', '綱', '酵',
              '豪', '穀', '酷', '獄', '魂', '際', '察', '雑', '算', '酸', '誌', '雌', '磁', '漆', '遮', '種', '需',
              '銃', '塾', '緒', '彰', '障', '精', '製', '誓', '静', '説', '箋', '銭', '漸', '遡', '層', '総', '遭',
              '像', '増', '憎', '遜', '駄', '態', '奪', '端', '綻', '嫡', '徴', '漬', '摘', '滴', '適', '稲', '銅',
              '徳', '読', '認', '寧', '髪', '罰', '閥', '碑', '鼻', '漂', '腐', '複', '聞', '蔑', '慕', '暮', '貌',
              '僕', '墨', '膜', '慢', '漫', '蜜', '銘', '鳴', '綿', '模', '網', '誘', '様', '瘍', '踊', '辣', '僚',
              '領', '緑', '瑠', '暦', '歴', '練', '漏', '慰', '遺', '影', '鋭', '謁', '閲', '縁', '横', '億', '稼',
              '課', '餓', '潰', '確', '潟', '歓', '監', '緩', '器', '畿', '輝', '儀', '戯', '窮', '緊', '勲', '慶',
              '憬', '稽', '劇', '撃', '潔', '権', '稿', '駒', '撮', '賛', '暫', '摯', '賜', '餌', '質', '趣', '熟',
              '潤', '遵', '諸', '憧', '衝', '賞', '縄', '嘱', '審', '震', '穂', '請', '潜', '線', '遷', '選', '槽',
              '踪', '蔵', '諾', '誰', '誕', '談', '鋳', '駐', '嘲', '潮', '澄', '調', '墜', '締', '敵', '徹', '撤',
              '踏', '導', '熱', '罵', '輩', '賠', '箱', '箸', '範', '盤', '罷', '膝', '標', '賓', '敷', '膚', '賦',
              '舞', '噴', '墳', '憤', '幣', '弊', '蔽', '餅', '編', '舗', '褒', '暴', '撲', '摩', '魅', '黙', '憂',
              '窯', '養', '履', '璃', '慮', '寮', '輪', '霊', '論', '緯', '衛', '憶', '穏', '壊', '懐', '諧', '骸',
              '獲', '憾', '還', '館', '機', '橋', '凝', '錦', '薫', '憩', '激', '憲', '賢', '錮', '興', '衡', '鋼',
              '墾', '錯', '諮', '儒', '樹', '獣', '縦', '壌', '嬢', '錠', '薪', '親', '整', '醒', '積', '薦', '膳',
              '操', '濁', '壇', '緻', '築', '諦', '賭', '糖', '頭', '篤', '曇', '燃', '濃', '薄', '縛', '繁', '避',
              '奮', '壁', '縫', '膨', '謀', '頰', '磨', '麺', '薬', '諭', '輸', '融', '擁', '謡', '頼', '隣', '隷',
              '錬', '録', '曖', '臆', '嚇', '轄', '環', '擬', '犠', '矯', '謹', '謙', '鍵', '厳', '講', '購', '懇',
              '擦', '謝', '爵', '醜', '縮', '償', '礁', '績', '繊', '鮮', '燥', '霜', '戴', '濯', '鍛', '聴', '謄',
              '瞳', '謎', '鍋', '頻', '闇', '優', '翼', '覧', '療', '瞭', '齢', '穫', '額', '顎', '鎌', '簡', '観',
              '韓', '顔', '騎', '襟', '繭', '顕', '験', '鎖', '瞬', '織', '職', '繕', '礎', '騒', '贈', '題', '懲',
              '鎮', '藤', '闘', '難', '藩', '覆', '璧', '癖', '翻', '癒', '曜', '濫', '藍', '糧', '臨', '類', '韻',
              '艶', '願', '鏡', '繰', '警', '鶏', '鯨', '璽', '識', '蹴', '髄', '瀬', '藻', '臓', '覇', '爆', '譜',
              '簿', '霧', '羅', '離', '麗', '麓', '議', '競', '響', '懸', '護', '鐘', '譲', '醸', '籍', '騰', '欄',
              '艦', '顧', '鶴', '魔', '躍', '露', '驚', '襲', '籠', '鑑', '鬱'));

  // Unicode BMP (Basic Multilingual Plane) lookup.
  // Character.MAX_VALUE has the Unicode code point U+FFFF, which is
  // the largest value of type char in Java (which uses UTF-16 encoding).
  private static final int[] JOUYOU_SET_UNICODE_POINTS = new int[Character.MAX_VALUE + 1];

  static {
    for (final Character character : JOUYOU_SET) {
      final int codePoint = (int) character;
      JOUYOU_SET_UNICODE_POINTS[codePoint] = codePoint;
    }

    // We do not need this anymore at runtime
    JOUYOU_SET.clear();
  }

  static boolean of(final int codePoint) {
    if (!Character.isValidCodePoint(codePoint) || codePoint >= JOUYOU_SET_UNICODE_POINTS.length) {
      return false;
    } else {
      return JOUYOU_SET_UNICODE_POINTS[codePoint] != 0;
    }
  }
}
