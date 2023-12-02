bool flw_discardPredicate(vec4 finalColor) {
    return finalColor.a < 0.01;
}
