package com.rehan.rangoli.domain

enum class Achievement(
	val id: String,
	val title: String,
	val description: String,
	val icon: String
) {
	FIRST_WIN(
		"first_win", "Pehli Jeet",
		"Pehla level poora kiya", "🎉"
	),
	PERFECT_SOLVE(
		"perfect", "Perfect Rangoli",
		"Bina galti aur hint ke clear karo", "⭐"
	),
	SPEED_STAR(
		"speed_star", "Speed Star",
		"60 second ke andar level clear karo", "⚡"
	),
	NO_HINT_TEN(
		"no_hint_10", "Khud Socha",
		"10 levels bina hint ke", "🧠"
	),
	HALFWAY(
		"halfway", "Aadha Rangoli",
		"50 levels complete karo", "🌸"
	),
	MASTER(
		"master", "Rangoli Master",
		"Sab 100 levels complete karo", "🏆"
	),
	STREAK_FIVE(
		"streak_5", "Lagataar Paanch",
		"5 consecutive levels bina galti ke", "🔥"
	),
	THREE_STARS_TEN(
		"stars_10", "Chamakdar",
		"10 levels mein 3 stars haasil karo", "✨"
	),
	CHAPTER_ONE(
		"chapter_1", "Pehla Chapter",
		"Aadha Aadha chapter (levels 1-15) poora karo", "🪞"
	),
	SPEED_TEN(
		"speed_10", "Bijli",
		"10 levels 60 sec ke andar clear karo", "🌩️"
	),
}
