#!/usr/bin/env node

var parser = require(__dirname + '/CLDRPluralRuleParser.js');
if (process.argv.length < 3) {
    console.log("Please provide the rule and a number to test");
    console.log("Example:");
    console.log("cldrpluraltuleparser 'v = 0 and n != 0..10 and n % 10 = 0' 20");
} else {
    var result = parser(process.argv[2], process.argv[3]);
    console.log(result);
}