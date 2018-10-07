package com.oopsjpeg.gacha.util;

import com.oopsjpeg.gacha.Util;
import com.oopsjpeg.gacha.data.impl.Card;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CardQuery {
	public static final int SORT_STAR = 0;

	public static final int FILTER_IDENTICAL = 0;

	private List<Card> cards;

	private CardQuery(List<Card> cards) {
		this.cards = cards;
	}

	public static CardQuery of(Collection<Card> cards) {
		return new CardQuery(new ArrayList<>(cards));
	}

	public CardQuery sort(int type) {
		Comparator<Card> compare;
		switch (type) {
			case SORT_STAR:
				compare = Comparator.comparingInt(Card::getStar).reversed();
				break;
			default: return this;
		}

		cards = cards.stream()
				.sorted(compare.thenComparing(Card::getName))
				.collect(Collectors.toList());

		return this;
	}

	public CardQuery filter(int type) {
		cards = cards.stream().filter(c -> {
			switch (type) {
				case FILTER_IDENTICAL: return cards.stream().filter(c::equals).count() >= 2;
				default: return false;
			}
		}).collect(Collectors.toList());

		return this;
	}

	public CardQuery page(int page) {
		cards = cards.stream().skip((page - 1) * 10).limit(10)
				.collect(Collectors.toList());

		return this;
	}

	public int pages() {
		return (int) Math.ceil((float) cards.size() / 10);
	}

	public List<Card> get() {
		return cards;
	}

	public String raw() {
		return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") "
				+ c.getName() + " [" + c.getID() + "]")
				.collect(Collectors.joining("\n"));
	}

	public String format() {
		return cards.stream().map(c -> "(" + Util.star(c.getStar()) + ") **"
				+ c.getName() + "** [`" + c.getID() + "`]")
				.collect(Collectors.joining("\n"));
	}
}