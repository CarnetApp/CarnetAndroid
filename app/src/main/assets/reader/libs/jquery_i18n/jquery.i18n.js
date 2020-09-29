"use strict";

function _typeof(obj) { if (typeof Symbol === "function" && typeof Symbol.iterator === "symbol") { _typeof = function _typeof(obj) { return typeof obj; }; } else { _typeof = function _typeof(obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; }; } return _typeof(obj); }

/*!
 * jQuery Internationalization library
 *
 * Copyright (C) 2012 Santhosh Thottingal
 *
 * jquery.i18n is dual licensed GPLv2 or later and MIT. You don't have to do
 * anything special to choose one license or the other and you don't have to
 * notify anyone which license you are using. You are free to use
 * UniversalLanguageSelector in commercial projects as long as the copyright
 * header is left intact. See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */
(function ($) {
  'use strict';

  var _I18N,
      slice = Array.prototype.slice;
  /**
   * @constructor
   * @param {Object} options
   */


  _I18N = function I18N(options) {
    // Load defaults
    this.options = $.extend({}, _I18N.defaults, options);
    this.parser = this.options.parser;
    this.locale = this.options.locale;
    this.messageStore = this.options.messageStore;
    this.languages = {};
  };

  _I18N.prototype = {
    /**
     * Localize a given messageKey to a locale.
     * @param {string} messageKey
     * @return {string} Localized message
     */
    localize: function localize(messageKey) {
      var localeParts, localePartIndex, locale, fallbackIndex, tryingLocale, message;
      locale = this.locale;
      fallbackIndex = 0;

      while (locale) {
        // Iterate through locales starting at most-specific until
        // localization is found. As in fi-Latn-FI, fi-Latn and fi.
        localeParts = locale.split('-');
        localePartIndex = localeParts.length;

        do {
          tryingLocale = localeParts.slice(0, localePartIndex).join('-');
          message = this.messageStore.get(tryingLocale, messageKey);

          if (message) {
            return message;
          }

          localePartIndex--;
        } while (localePartIndex);

        if (locale === this.options.fallbackLocale) {
          break;
        }

        locale = $.i18n.fallbacks[this.locale] && $.i18n.fallbacks[this.locale][fallbackIndex] || this.options.fallbackLocale;
        $.i18n.log('Trying fallback locale for ' + this.locale + ': ' + locale + ' (' + messageKey + ')');
        fallbackIndex++;
      } // key not found


      return '';
    },

    /*
     * Destroy the i18n instance.
     */
    destroy: function destroy() {
      $.removeData(document, 'i18n');
    },

    /**
     * General message loading API This can take a URL string for
     * the json formatted messages. Example:
     * <code>load('path/to/all_localizations.json');</code>
     *
     * To load a localization file for a locale:
     * <code>
     * load('path/to/de-messages.json', 'de' );
     * </code>
     *
     * To load a localization file from a directory:
     * <code>
     * load('path/to/i18n/directory', 'de' );
     * </code>
     * The above method has the advantage of fallback resolution.
     * ie, it will automatically load the fallback locales for de.
     * For most usecases, this is the recommended method.
     * It is optional to have trailing slash at end.
     *
     * A data object containing message key- message translation mappings
     * can also be passed. Example:
     * <code>
     * load( { 'hello' : 'Hello' }, optionalLocale );
     * </code>
     *
     * A source map containing key-value pair of languagename and locations
     * can also be passed. Example:
     * <code>
     * load( {
     * bn: 'i18n/bn.json',
     * he: 'i18n/he.json',
     * en: 'i18n/en.json'
     * } )
     * </code>
     *
     * If the data argument is null/undefined/false,
     * all cached messages for the i18n instance will get reset.
     *
     * @param {string|Object} source
     * @param {string} locale Language tag
     * @return {jQuery.Promise}
     */
    load: function load(source, locale) {
      var fallbackLocales,
          locIndex,
          fallbackLocale,
          sourceMap = {};

      if (!source && !locale) {
        source = 'i18n/' + $.i18n().locale + '.json';
        locale = $.i18n().locale;
      }

      if (typeof source === 'string' && // source extension should be json, but can have query params after that.
      source.split('?')[0].split('.').pop() !== 'json') {
        // Load specified locale then check for fallbacks when directory is
        // specified in load()
        sourceMap[locale] = source + '/' + locale + '.json';
        fallbackLocales = ($.i18n.fallbacks[locale] || []).concat(this.options.fallbackLocale);

        for (locIndex = 0; locIndex < fallbackLocales.length; locIndex++) {
          fallbackLocale = fallbackLocales[locIndex];
          sourceMap[fallbackLocale] = source + '/' + fallbackLocale + '.json';
        }

        return this.load(sourceMap);
      } else {
        return this.messageStore.load(source, locale);
      }
    },

    /**
     * Does parameter and magic word substitution.
     *
     * @param {string} key Message key
     * @param {Array} parameters Message parameters
     * @return {string}
     */
    parse: function parse(key, parameters) {
      var message = this.localize(key); // FIXME: This changes the state of the I18N object,
      // should probably not change the 'this.parser' but just
      // pass it to the parser.

      this.parser.language = $.i18n.languages[$.i18n().locale] || $.i18n.languages['default'];

      if (message === '') {
        message = key;
      }

      return this.parser.parse(message, parameters);
    }
  };
  /**
   * Process a message from the $.I18N instance
   * for the current document, stored in jQuery.data(document).
   *
   * @param {string} key Key of the message.
   * @param {string} param1 [param...] Variadic list of parameters for {key}.
   * @return {string|$.I18N} Parsed message, or if no key was given
   * the instance of $.I18N is returned.
   */

  $.i18n = function (key, param1) {
    var parameters,
        i18n = $.data(document, 'i18n'),
        options = _typeof(key) === 'object' && key; // If the locale option for this call is different then the setup so far,
    // update it automatically. This doesn't just change the context for this
    // call but for all future call as well.
    // If there is no i18n setup yet, don't do this. It will be taken care of
    // by the `new I18N` construction below.
    // NOTE: It should only change language for this one call.
    // Then cache instances of I18N somewhere.

    if (options && options.locale && i18n && i18n.locale !== options.locale) {
      i18n.locale = options.locale;
    }

    if (!i18n) {
      i18n = new _I18N(options);
      $.data(document, 'i18n', i18n);
    }

    if (typeof key === 'string') {
      if (param1 !== undefined) {
        parameters = slice.call(arguments, 1);
      } else {
        parameters = [];
      }

      return i18n.parse(key, parameters);
    } else {
      // FIXME: remove this feature/bug.
      return i18n;
    }
  };

  $.fn.i18n = function () {
    var i18n = $.data(document, 'i18n');

    if (!i18n) {
      i18n = new _I18N();
      $.data(document, 'i18n', i18n);
    }

    return this.each(function () {
      var $this = $(this),
          messageKey = $this.data('i18n'),
          lBracket,
          rBracket,
          type,
          key;

      if (messageKey) {
        lBracket = messageKey.indexOf('[');
        rBracket = messageKey.indexOf(']');

        if (lBracket !== -1 && rBracket !== -1 && lBracket < rBracket) {
          type = messageKey.slice(lBracket + 1, rBracket);
          key = messageKey.slice(rBracket + 1);

          if (type === 'html') {
            $this.html(i18n.parse(key));
          } else {
            $this.attr(type, i18n.parse(key));
          }
        } else {
          $this.text(i18n.parse(messageKey));
        }
      } else {
        $this.find('[data-i18n]').i18n();
      }
    });
  };

  function getDefaultLocale() {
    var locale = $('html').attr('lang');

    if (!locale) {
      locale = navigator.language || navigator.userLanguage || '';
    }

    return locale;
  }

  $.i18n.languages = {};
  $.i18n.messageStore = $.i18n.messageStore || {};
  $.i18n.parser = {
    // The default parser only handles variable substitution
    parse: function parse(message, parameters) {
      return message.replace(/\$(\d+)/g, function (str, match) {
        var index = parseInt(match, 10) - 1;
        return parameters[index] !== undefined ? parameters[index] : '$' + match;
      });
    },
    emitter: {}
  };
  $.i18n.fallbacks = {};
  $.i18n.debug = false;

  $.i18n.log = function ()
  /* arguments */
  {
    if (window.console && $.i18n.debug) {
      window.console.log.apply(window.console, arguments);
    }
  };
  /* Static members */


  _I18N.defaults = {
    locale: getDefaultLocale(),
    fallbackLocale: 'en',
    parser: $.i18n.parser,
    messageStore: $.i18n.messageStore
  }; // Expose constructor

  $.i18n.constructor = _I18N;
})(jQuery);
/*!
 * jQuery Internationalization library
 *
 * Copyright (C) 2011-2013 Santhosh Thottingal, Neil Kandalgaonkar
 *
 * jquery.i18n is dual licensed GPLv2 or later and MIT. You don't have to do
 * anything special to choose one license or the other and you don't have to
 * notify anyone which license you are using. You are free to use
 * UniversalLanguageSelector in commercial projects as long as the copyright
 * header is left intact. See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */


(function ($) {
  'use strict';

  var MessageParserEmitter = function MessageParserEmitter() {
    this.language = $.i18n.languages[String.locale] || $.i18n.languages['default'];
  };

  MessageParserEmitter.prototype = {
    constructor: MessageParserEmitter,

    /**
     * (We put this method definition here, and not in prototype, to make
     * sure it's not overwritten by any magic.) Walk entire node structure,
     * applying replacements and template functions when appropriate
     *
     * @param {Mixed} node abstract syntax tree (top node or subnode)
     * @param {Array} replacements for $1, $2, ... $n
     * @return {Mixed} single-string node or array of nodes suitable for
     *  jQuery appending.
     */
    emit: function emit(node, replacements) {
      var ret,
          subnodes,
          operation,
          messageParserEmitter = this;

      switch (_typeof(node)) {
        case 'string':
        case 'number':
          ret = node;
          break;

        case 'object':
          // node is an array of nodes
          subnodes = $.map(node.slice(1), function (n) {
            return messageParserEmitter.emit(n, replacements);
          });
          operation = node[0].toLowerCase();

          if (typeof messageParserEmitter[operation] === 'function') {
            ret = messageParserEmitter[operation](subnodes, replacements);
          } else {
            throw new Error('unknown operation "' + operation + '"');
          }

          break;

        case 'undefined':
          // Parsing the empty string (as an entire expression, or as a
          // paramExpression in a template) results in undefined
          // Perhaps a more clever parser can detect this, and return the
          // empty string? Or is that useful information?
          // The logical thing is probably to return the empty string here
          // when we encounter undefined.
          ret = '';
          break;

        default:
          throw new Error('unexpected type in AST: ' + _typeof(node));
      }

      return ret;
    },

    /**
     * Parsing has been applied depth-first we can assume that all nodes
     * here are single nodes Must return a single node to parents -- a
     * jQuery with synthetic span However, unwrap any other synthetic spans
     * in our children and pass them upwards
     *
     * @param {Array} nodes Mixed, some single nodes, some arrays of nodes.
     * @return {string}
     */
    concat: function concat(nodes) {
      var result = '';
      $.each(nodes, function (i, node) {
        // strings, integers, anything else
        result += node;
      });
      return result;
    },

    /**
     * Return escaped replacement of correct index, or string if
     * unavailable. Note that we expect the parsed parameter to be
     * zero-based. i.e. $1 should have become [ 0 ]. if the specified
     * parameter is not found return the same string (e.g. "$99" ->
     * parameter 98 -> not found -> return "$99" ) TODO throw error if
     * nodes.length > 1 ?
     *
     * @param {Array} nodes One element, integer, n >= 0
     * @param {Array} replacements for $1, $2, ... $n
     * @return {string} replacement
     */
    replace: function replace(nodes, replacements) {
      var index = parseInt(nodes[0], 10);

      if (index < replacements.length) {
        // replacement is not a string, don't touch!
        return replacements[index];
      } else {
        // index not found, fallback to displaying variable
        return '$' + (index + 1);
      }
    },

    /**
     * Transform parsed structure into pluralization n.b. The first node may
     * be a non-integer (for instance, a string representing an Arabic
     * number). So convert it back with the current language's
     * convertNumber.
     *
     * @param {Array} nodes List [ {String|Number}, {String}, {String} ... ]
     * @return {string} selected pluralized form according to current
     *  language.
     */
    plural: function plural(nodes) {
      var count = parseFloat(this.language.convertNumber(nodes[0], 10)),
          forms = nodes.slice(1);
      return forms.length ? this.language.convertPlural(count, forms) : '';
    },

    /**
     * Transform parsed structure into gender Usage
     * {{gender:gender|masculine|feminine|neutral}}.
     *
     * @param {Array} nodes List [ {String}, {String}, {String} , {String} ]
     * @return {string} selected gender form according to current language
     */
    gender: function gender(nodes) {
      var gender = nodes[0],
          forms = nodes.slice(1);
      return this.language.gender(gender, forms);
    },

    /**
     * Transform parsed structure into grammar conversion. Invoked by
     * putting {{grammar:form|word}} in a message
     *
     * @param {Array} nodes List [{Grammar case eg: genitive}, {String word}]
     * @return {string} selected grammatical form according to current
     *  language.
     */
    grammar: function grammar(nodes) {
      var form = nodes[0],
          word = nodes[1];
      return word && form && this.language.convertGrammar(word, form);
    }
  };
  $.extend($.i18n.parser.emitter, new MessageParserEmitter());
})(jQuery);
/*!
 * BIDI embedding support for jQuery.i18n
 *
 * Copyright (C) 2015, David Chan
 *
 * This code is dual licensed GPLv2 or later and MIT. You don't have to do
 * anything special to choose one license or the other and you don't have to
 * notify anyone which license you are using. You are free to use this code
 * in commercial projects as long as the copyright header is left intact.
 * See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */


(function ($) {
  'use strict';

  var strongDirRegExp;
  /**
   * Matches the first strong directionality codepoint:
   * - in group 1 if it is LTR
   * - in group 2 if it is RTL
   * Does not match if there is no strong directionality codepoint.
   *
   * Generated by UnicodeJS (see tools/strongDir) from the UCD; see
   * https://phabricator.wikimedia.org/diffusion/GUJS/ .
   */

  strongDirRegExp = new RegExp('(?:' + '(' + "[A-Za-z\xAA\xB5\xBA\xC0-\xD6\xD8-\xF6\xF8-\u02B8\u02BB-\u02C1\u02D0\u02D1\u02E0-\u02E4\u02EE\u0370-\u0373\u0376\u0377\u037A-\u037D\u037F\u0386\u0388-\u038A\u038C\u038E-\u03A1\u03A3-\u03F5\u03F7-\u0482\u048A-\u052F\u0531-\u0556\u0559-\u055F\u0561-\u0587\u0589\u0903-\u0939\u093B\u093D-\u0940\u0949-\u094C\u094E-\u0950\u0958-\u0961\u0964-\u0980\u0982\u0983\u0985-\u098C\u098F\u0990\u0993-\u09A8\u09AA-\u09B0\u09B2\u09B6-\u09B9\u09BD-\u09C0\u09C7\u09C8\u09CB\u09CC\u09CE\u09D7\u09DC\u09DD\u09DF-\u09E1\u09E6-\u09F1\u09F4-\u09FA\u0A03\u0A05-\u0A0A\u0A0F\u0A10\u0A13-\u0A28\u0A2A-\u0A30\u0A32\u0A33\u0A35\u0A36\u0A38\u0A39\u0A3E-\u0A40\u0A59-\u0A5C\u0A5E\u0A66-\u0A6F\u0A72-\u0A74\u0A83\u0A85-\u0A8D\u0A8F-\u0A91\u0A93-\u0AA8\u0AAA-\u0AB0\u0AB2\u0AB3\u0AB5-\u0AB9\u0ABD-\u0AC0\u0AC9\u0ACB\u0ACC\u0AD0\u0AE0\u0AE1\u0AE6-\u0AF0\u0AF9\u0B02\u0B03\u0B05-\u0B0C\u0B0F\u0B10\u0B13-\u0B28\u0B2A-\u0B30\u0B32\u0B33\u0B35-\u0B39\u0B3D\u0B3E\u0B40\u0B47\u0B48\u0B4B\u0B4C\u0B57\u0B5C\u0B5D\u0B5F-\u0B61\u0B66-\u0B77\u0B83\u0B85-\u0B8A\u0B8E-\u0B90\u0B92-\u0B95\u0B99\u0B9A\u0B9C\u0B9E\u0B9F\u0BA3\u0BA4\u0BA8-\u0BAA\u0BAE-\u0BB9\u0BBE\u0BBF\u0BC1\u0BC2\u0BC6-\u0BC8\u0BCA-\u0BCC\u0BD0\u0BD7\u0BE6-\u0BF2\u0C01-\u0C03\u0C05-\u0C0C\u0C0E-\u0C10\u0C12-\u0C28\u0C2A-\u0C39\u0C3D\u0C41-\u0C44\u0C58-\u0C5A\u0C60\u0C61\u0C66-\u0C6F\u0C7F\u0C82\u0C83\u0C85-\u0C8C\u0C8E-\u0C90\u0C92-\u0CA8\u0CAA-\u0CB3\u0CB5-\u0CB9\u0CBD-\u0CC4\u0CC6-\u0CC8\u0CCA\u0CCB\u0CD5\u0CD6\u0CDE\u0CE0\u0CE1\u0CE6-\u0CEF\u0CF1\u0CF2\u0D02\u0D03\u0D05-\u0D0C\u0D0E-\u0D10\u0D12-\u0D3A\u0D3D-\u0D40\u0D46-\u0D48\u0D4A-\u0D4C\u0D4E\u0D57\u0D5F-\u0D61\u0D66-\u0D75\u0D79-\u0D7F\u0D82\u0D83\u0D85-\u0D96\u0D9A-\u0DB1\u0DB3-\u0DBB\u0DBD\u0DC0-\u0DC6\u0DCF-\u0DD1\u0DD8-\u0DDF\u0DE6-\u0DEF\u0DF2-\u0DF4\u0E01-\u0E30\u0E32\u0E33\u0E40-\u0E46\u0E4F-\u0E5B\u0E81\u0E82\u0E84\u0E87\u0E88\u0E8A\u0E8D\u0E94-\u0E97\u0E99-\u0E9F\u0EA1-\u0EA3\u0EA5\u0EA7\u0EAA\u0EAB\u0EAD-\u0EB0\u0EB2\u0EB3\u0EBD\u0EC0-\u0EC4\u0EC6\u0ED0-\u0ED9\u0EDC-\u0EDF\u0F00-\u0F17\u0F1A-\u0F34\u0F36\u0F38\u0F3E-\u0F47\u0F49-\u0F6C\u0F7F\u0F85\u0F88-\u0F8C\u0FBE-\u0FC5\u0FC7-\u0FCC\u0FCE-\u0FDA\u1000-\u102C\u1031\u1038\u103B\u103C\u103F-\u1057\u105A-\u105D\u1061-\u1070\u1075-\u1081\u1083\u1084\u1087-\u108C\u108E-\u109C\u109E-\u10C5\u10C7\u10CD\u10D0-\u1248\u124A-\u124D\u1250-\u1256\u1258\u125A-\u125D\u1260-\u1288\u128A-\u128D\u1290-\u12B0\u12B2-\u12B5\u12B8-\u12BE\u12C0\u12C2-\u12C5\u12C8-\u12D6\u12D8-\u1310\u1312-\u1315\u1318-\u135A\u1360-\u137C\u1380-\u138F\u13A0-\u13F5\u13F8-\u13FD\u1401-\u167F\u1681-\u169A\u16A0-\u16F8\u1700-\u170C\u170E-\u1711\u1720-\u1731\u1735\u1736\u1740-\u1751\u1760-\u176C\u176E-\u1770\u1780-\u17B3\u17B6\u17BE-\u17C5\u17C7\u17C8\u17D4-\u17DA\u17DC\u17E0-\u17E9\u1810-\u1819\u1820-\u1877\u1880-\u18A8\u18AA\u18B0-\u18F5\u1900-\u191E\u1923-\u1926\u1929-\u192B\u1930\u1931\u1933-\u1938\u1946-\u196D\u1970-\u1974\u1980-\u19AB\u19B0-\u19C9\u19D0-\u19DA\u1A00-\u1A16\u1A19\u1A1A\u1A1E-\u1A55\u1A57\u1A61\u1A63\u1A64\u1A6D-\u1A72\u1A80-\u1A89\u1A90-\u1A99\u1AA0-\u1AAD\u1B04-\u1B33\u1B35\u1B3B\u1B3D-\u1B41\u1B43-\u1B4B\u1B50-\u1B6A\u1B74-\u1B7C\u1B82-\u1BA1\u1BA6\u1BA7\u1BAA\u1BAE-\u1BE5\u1BE7\u1BEA-\u1BEC\u1BEE\u1BF2\u1BF3\u1BFC-\u1C2B\u1C34\u1C35\u1C3B-\u1C49\u1C4D-\u1C7F\u1CC0-\u1CC7\u1CD3\u1CE1\u1CE9-\u1CEC\u1CEE-\u1CF3\u1CF5\u1CF6\u1D00-\u1DBF\u1E00-\u1F15\u1F18-\u1F1D\u1F20-\u1F45\u1F48-\u1F4D\u1F50-\u1F57\u1F59\u1F5B\u1F5D\u1F5F-\u1F7D\u1F80-\u1FB4\u1FB6-\u1FBC\u1FBE\u1FC2-\u1FC4\u1FC6-\u1FCC\u1FD0-\u1FD3\u1FD6-\u1FDB\u1FE0-\u1FEC\u1FF2-\u1FF4\u1FF6-\u1FFC\u200E\u2071\u207F\u2090-\u209C\u2102\u2107\u210A-\u2113\u2115\u2119-\u211D\u2124\u2126\u2128\u212A-\u212D\u212F-\u2139\u213C-\u213F\u2145-\u2149\u214E\u214F\u2160-\u2188\u2336-\u237A\u2395\u249C-\u24E9\u26AC\u2800-\u28FF\u2C00-\u2C2E\u2C30-\u2C5E\u2C60-\u2CE4\u2CEB-\u2CEE\u2CF2\u2CF3\u2D00-\u2D25\u2D27\u2D2D\u2D30-\u2D67\u2D6F\u2D70\u2D80-\u2D96\u2DA0-\u2DA6\u2DA8-\u2DAE\u2DB0-\u2DB6\u2DB8-\u2DBE\u2DC0-\u2DC6\u2DC8-\u2DCE\u2DD0-\u2DD6\u2DD8-\u2DDE\u3005-\u3007\u3021-\u3029\u302E\u302F\u3031-\u3035\u3038-\u303C\u3041-\u3096\u309D-\u309F\u30A1-\u30FA\u30FC-\u30FF\u3105-\u312D\u3131-\u318E\u3190-\u31BA\u31F0-\u321C\u3220-\u324F\u3260-\u327B\u327F-\u32B0\u32C0-\u32CB\u32D0-\u32FE\u3300-\u3376\u337B-\u33DD\u33E0-\u33FE\u3400-\u4DB5\u4E00-\u9FD5\uA000-\uA48C\uA4D0-\uA60C\uA610-\uA62B\uA640-\uA66E\uA680-\uA69D\uA6A0-\uA6EF\uA6F2-\uA6F7\uA722-\uA787\uA789-\uA7AD\uA7B0-\uA7B7\uA7F7-\uA801\uA803-\uA805\uA807-\uA80A\uA80C-\uA824\uA827\uA830-\uA837\uA840-\uA873\uA880-\uA8C3\uA8CE-\uA8D9\uA8F2-\uA8FD\uA900-\uA925\uA92E-\uA946\uA952\uA953\uA95F-\uA97C\uA983-\uA9B2\uA9B4\uA9B5\uA9BA\uA9BB\uA9BD-\uA9CD\uA9CF-\uA9D9\uA9DE-\uA9E4\uA9E6-\uA9FE\uAA00-\uAA28\uAA2F\uAA30\uAA33\uAA34\uAA40-\uAA42\uAA44-\uAA4B\uAA4D\uAA50-\uAA59\uAA5C-\uAA7B\uAA7D-\uAAAF\uAAB1\uAAB5\uAAB6\uAAB9-\uAABD\uAAC0\uAAC2\uAADB-\uAAEB\uAAEE-\uAAF5\uAB01-\uAB06\uAB09-\uAB0E\uAB11-\uAB16\uAB20-\uAB26\uAB28-\uAB2E\uAB30-\uAB65\uAB70-\uABE4\uABE6\uABE7\uABE9-\uABEC\uABF0-\uABF9\uAC00-\uD7A3\uD7B0-\uD7C6\uD7CB-\uD7FB\uE000-\uFA6D\uFA70-\uFAD9\uFB00-\uFB06\uFB13-\uFB17\uFF21-\uFF3A\uFF41-\uFF5A\uFF66-\uFFBE\uFFC2-\uFFC7\uFFCA-\uFFCF\uFFD2-\uFFD7\uFFDA-\uFFDC]|\uD800[\uDC00-\uDC0B]|\uD800[\uDC0D-\uDC26]|\uD800[\uDC28-\uDC3A]|\uD800\uDC3C|\uD800\uDC3D|\uD800[\uDC3F-\uDC4D]|\uD800[\uDC50-\uDC5D]|\uD800[\uDC80-\uDCFA]|\uD800\uDD00|\uD800\uDD02|\uD800[\uDD07-\uDD33]|\uD800[\uDD37-\uDD3F]|\uD800[\uDDD0-\uDDFC]|\uD800[\uDE80-\uDE9C]|\uD800[\uDEA0-\uDED0]|\uD800[\uDF00-\uDF23]|\uD800[\uDF30-\uDF4A]|\uD800[\uDF50-\uDF75]|\uD800[\uDF80-\uDF9D]|\uD800[\uDF9F-\uDFC3]|\uD800[\uDFC8-\uDFD5]|\uD801[\uDC00-\uDC9D]|\uD801[\uDCA0-\uDCA9]|\uD801[\uDD00-\uDD27]|\uD801[\uDD30-\uDD63]|\uD801\uDD6F|\uD801[\uDE00-\uDF36]|\uD801[\uDF40-\uDF55]|\uD801[\uDF60-\uDF67]|\uD804\uDC00|\uD804[\uDC02-\uDC37]|\uD804[\uDC47-\uDC4D]|\uD804[\uDC66-\uDC6F]|\uD804[\uDC82-\uDCB2]|\uD804\uDCB7|\uD804\uDCB8|\uD804[\uDCBB-\uDCC1]|\uD804[\uDCD0-\uDCE8]|\uD804[\uDCF0-\uDCF9]|\uD804[\uDD03-\uDD26]|\uD804\uDD2C|\uD804[\uDD36-\uDD43]|\uD804[\uDD50-\uDD72]|\uD804[\uDD74-\uDD76]|\uD804[\uDD82-\uDDB5]|\uD804[\uDDBF-\uDDC9]|\uD804\uDDCD|\uD804[\uDDD0-\uDDDF]|\uD804[\uDDE1-\uDDF4]|\uD804[\uDE00-\uDE11]|\uD804[\uDE13-\uDE2E]|\uD804\uDE32|\uD804\uDE33|\uD804\uDE35|\uD804[\uDE38-\uDE3D]|\uD804[\uDE80-\uDE86]|\uD804\uDE88|\uD804[\uDE8A-\uDE8D]|\uD804[\uDE8F-\uDE9D]|\uD804[\uDE9F-\uDEA9]|\uD804[\uDEB0-\uDEDE]|\uD804[\uDEE0-\uDEE2]|\uD804[\uDEF0-\uDEF9]|\uD804\uDF02|\uD804\uDF03|\uD804[\uDF05-\uDF0C]|\uD804\uDF0F|\uD804\uDF10|\uD804[\uDF13-\uDF28]|\uD804[\uDF2A-\uDF30]|\uD804\uDF32|\uD804\uDF33|\uD804[\uDF35-\uDF39]|\uD804[\uDF3D-\uDF3F]|\uD804[\uDF41-\uDF44]|\uD804\uDF47|\uD804\uDF48|\uD804[\uDF4B-\uDF4D]|\uD804\uDF50|\uD804\uDF57|\uD804[\uDF5D-\uDF63]|\uD805[\uDC80-\uDCB2]|\uD805\uDCB9|\uD805[\uDCBB-\uDCBE]|\uD805\uDCC1|\uD805[\uDCC4-\uDCC7]|\uD805[\uDCD0-\uDCD9]|\uD805[\uDD80-\uDDB1]|\uD805[\uDDB8-\uDDBB]|\uD805\uDDBE|\uD805[\uDDC1-\uDDDB]|\uD805[\uDE00-\uDE32]|\uD805\uDE3B|\uD805\uDE3C|\uD805\uDE3E|\uD805[\uDE41-\uDE44]|\uD805[\uDE50-\uDE59]|\uD805[\uDE80-\uDEAA]|\uD805\uDEAC|\uD805\uDEAE|\uD805\uDEAF|\uD805\uDEB6|\uD805[\uDEC0-\uDEC9]|\uD805[\uDF00-\uDF19]|\uD805\uDF20|\uD805\uDF21|\uD805\uDF26|\uD805[\uDF30-\uDF3F]|\uD806[\uDCA0-\uDCF2]|\uD806\uDCFF|\uD806[\uDEC0-\uDEF8]|\uD808[\uDC00-\uDF99]|\uD809[\uDC00-\uDC6E]|\uD809[\uDC70-\uDC74]|\uD809[\uDC80-\uDD43]|\uD80C[\uDC00-\uDFFF]|\uD80D[\uDC00-\uDC2E]|\uD811[\uDC00-\uDE46]|\uD81A[\uDC00-\uDE38]|\uD81A[\uDE40-\uDE5E]|\uD81A[\uDE60-\uDE69]|\uD81A\uDE6E|\uD81A\uDE6F|\uD81A[\uDED0-\uDEED]|\uD81A\uDEF5|\uD81A[\uDF00-\uDF2F]|\uD81A[\uDF37-\uDF45]|\uD81A[\uDF50-\uDF59]|\uD81A[\uDF5B-\uDF61]|\uD81A[\uDF63-\uDF77]|\uD81A[\uDF7D-\uDF8F]|\uD81B[\uDF00-\uDF44]|\uD81B[\uDF50-\uDF7E]|\uD81B[\uDF93-\uDF9F]|\uD82C\uDC00|\uD82C\uDC01|\uD82F[\uDC00-\uDC6A]|\uD82F[\uDC70-\uDC7C]|\uD82F[\uDC80-\uDC88]|\uD82F[\uDC90-\uDC99]|\uD82F\uDC9C|\uD82F\uDC9F|\uD834[\uDC00-\uDCF5]|\uD834[\uDD00-\uDD26]|\uD834[\uDD29-\uDD66]|\uD834[\uDD6A-\uDD72]|\uD834\uDD83|\uD834\uDD84|\uD834[\uDD8C-\uDDA9]|\uD834[\uDDAE-\uDDE8]|\uD834[\uDF60-\uDF71]|\uD835[\uDC00-\uDC54]|\uD835[\uDC56-\uDC9C]|\uD835\uDC9E|\uD835\uDC9F|\uD835\uDCA2|\uD835\uDCA5|\uD835\uDCA6|\uD835[\uDCA9-\uDCAC]|\uD835[\uDCAE-\uDCB9]|\uD835\uDCBB|\uD835[\uDCBD-\uDCC3]|\uD835[\uDCC5-\uDD05]|\uD835[\uDD07-\uDD0A]|\uD835[\uDD0D-\uDD14]|\uD835[\uDD16-\uDD1C]|\uD835[\uDD1E-\uDD39]|\uD835[\uDD3B-\uDD3E]|\uD835[\uDD40-\uDD44]|\uD835\uDD46|\uD835[\uDD4A-\uDD50]|\uD835[\uDD52-\uDEA5]|\uD835[\uDEA8-\uDEDA]|\uD835[\uDEDC-\uDF14]|\uD835[\uDF16-\uDF4E]|\uD835[\uDF50-\uDF88]|\uD835[\uDF8A-\uDFC2]|\uD835[\uDFC4-\uDFCB]|\uD836[\uDC00-\uDDFF]|\uD836[\uDE37-\uDE3A]|\uD836[\uDE6D-\uDE74]|\uD836[\uDE76-\uDE83]|\uD836[\uDE85-\uDE8B]|\uD83C[\uDD10-\uDD2E]|\uD83C[\uDD30-\uDD69]|\uD83C[\uDD70-\uDD9A]|\uD83C[\uDDE6-\uDE02]|\uD83C[\uDE10-\uDE3A]|\uD83C[\uDE40-\uDE48]|\uD83C\uDE50|\uD83C\uDE51|[\uD840-\uD868][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6]|\uD869[\uDF00-\uDFFF]|[\uD86A-\uD86C][\uDC00-\uDFFF]|\uD86D[\uDC00-\uDF34]|\uD86D[\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D]|\uD86E[\uDC20-\uDFFF]|[\uD86F-\uD872][\uDC00-\uDFFF]|\uD873[\uDC00-\uDEA1]|\uD87E[\uDC00-\uDE1D]|[\uDB80-\uDBBE][\uDC00-\uDFFF]|\uDBBF[\uDC00-\uDFFD]|[\uDBC0-\uDBFE][\uDC00-\uDFFF]|\uDBFF[\uDC00-\uDFFD]" + ')|(' + "[\u0590\u05BE\u05C0\u05C3\u05C6\u05C8-\u05FF\u07C0-\u07EA\u07F4\u07F5\u07FA-\u0815\u081A\u0824\u0828\u082E-\u0858\u085C-\u089F\u200F\uFB1D\uFB1F-\uFB28\uFB2A-\uFB4F\u0608\u060B\u060D\u061B-\u064A\u066D-\u066F\u0671-\u06D5\u06E5\u06E6\u06EE\u06EF\u06FA-\u0710\u0712-\u072F\u074B-\u07A5\u07B1-\u07BF\u08A0-\u08E2\uFB50-\uFD3D\uFD40-\uFDCF\uFDF0-\uFDFC\uFDFE\uFDFF\uFE70-\uFEFE]|\uD802[\uDC00-\uDD1E]|\uD802[\uDD20-\uDE00]|\uD802\uDE04|\uD802[\uDE07-\uDE0B]|\uD802[\uDE10-\uDE37]|\uD802[\uDE3B-\uDE3E]|\uD802[\uDE40-\uDEE4]|\uD802[\uDEE7-\uDF38]|\uD802[\uDF40-\uDFFF]|\uD803[\uDC00-\uDE5F]|\uD803[\uDE7F-\uDFFF]|\uD83A[\uDC00-\uDCCF]|\uD83A[\uDCD7-\uDFFF]|\uD83B[\uDC00-\uDDFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDF00-\uDFFF]|\uD83B[\uDE00-\uDEEF]|\uD83B[\uDEF2-\uDEFF]" + ')' + ')');
  /**
   * Gets directionality of the first strongly directional codepoint
   *
   * This is the rule the BIDI algorithm uses to determine the directionality of
   * paragraphs ( http://unicode.org/reports/tr9/#The_Paragraph_Level ) and
   * FSI isolates ( http://unicode.org/reports/tr9/#Explicit_Directional_Isolates ).
   *
   * TODO: Does not handle BIDI control characters inside the text.
   * TODO: Does not handle unallocated characters.
   *
   * @param {string} text The text from which to extract initial directionality.
   * @return {string} Directionality (either 'ltr' or 'rtl')
   */

  function strongDirFromContent(text) {
    var m = text.match(strongDirRegExp);

    if (!m) {
      return null;
    }

    if (m[2] === undefined) {
      return 'ltr';
    }

    return 'rtl';
  }

  $.extend($.i18n.parser.emitter, {
    /**
     * Wraps argument with unicode control characters for directionality safety
     *
     * This solves the problem where directionality-neutral characters at the edge of
     * the argument string get interpreted with the wrong directionality from the
     * enclosing context, giving renderings that look corrupted like "(Ben_(WMF".
     *
     * The wrapping is LRE...PDF or RLE...PDF, depending on the detected
     * directionality of the argument string, using the BIDI algorithm's own "First
     * strong directional codepoint" rule. Essentially, this works round the fact that
     * there is no embedding equivalent of U+2068 FSI (isolation with heuristic
     * direction inference). The latter is cleaner but still not widely supported.
     *
     * @param {string[]} nodes The text nodes from which to take the first item.
     * @return {string} Wrapped String of content as needed.
     */
    bidi: function bidi(nodes) {
      var dir = strongDirFromContent(nodes[0]);

      if (dir === 'ltr') {
        // Wrap in LEFT-TO-RIGHT EMBEDDING ... POP DIRECTIONAL FORMATTING
        return "\u202A" + nodes[0] + "\u202C";
      }

      if (dir === 'rtl') {
        // Wrap in RIGHT-TO-LEFT EMBEDDING ... POP DIRECTIONAL FORMATTING
        return "\u202B" + nodes[0] + "\u202C";
      } // No strong directionality: do not wrap


      return nodes[0];
    }
  });
})(jQuery);
/*!
 * jQuery Internationalization library
 *
 * Copyright (C) 2012 Santhosh Thottingal
 *
 * jquery.i18n is dual licensed GPLv2 or later and MIT. You don't have to do anything special to
 * choose one license or the other and you don't have to notify anyone which license you are using.
 * You are free to use UniversalLanguageSelector in commercial projects as long as the copyright
 * header is left intact. See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */


(function ($) {
  'use strict';

  $.i18n = $.i18n || {};
  $.extend($.i18n.fallbacks, {
    ab: ['ru'],
    ace: ['id'],
    aln: ['sq'],
    // Not so standard - als is supposed to be Tosk Albanian,
    // but in Wikipedia it's used for a Germanic language.
    als: ['gsw', 'de'],
    an: ['es'],
    anp: ['hi'],
    arn: ['es'],
    arz: ['ar'],
    av: ['ru'],
    ay: ['es'],
    ba: ['ru'],
    bar: ['de'],
    'bat-smg': ['sgs', 'lt'],
    bcc: ['fa'],
    'be-x-old': ['be-tarask'],
    bh: ['bho'],
    bjn: ['id'],
    bm: ['fr'],
    bpy: ['bn'],
    bqi: ['fa'],
    bug: ['id'],
    'cbk-zam': ['es'],
    ce: ['ru'],
    crh: ['crh-latn'],
    'crh-cyrl': ['ru'],
    csb: ['pl'],
    cv: ['ru'],
    'de-at': ['de'],
    'de-ch': ['de'],
    'de-formal': ['de'],
    dsb: ['de'],
    dtp: ['ms'],
    egl: ['it'],
    eml: ['it'],
    ff: ['fr'],
    fit: ['fi'],
    'fiu-vro': ['vro', 'et'],
    frc: ['fr'],
    frp: ['fr'],
    frr: ['de'],
    fur: ['it'],
    gag: ['tr'],
    gan: ['gan-hant', 'zh-hant', 'zh-hans'],
    'gan-hans': ['zh-hans'],
    'gan-hant': ['zh-hant', 'zh-hans'],
    gl: ['pt'],
    glk: ['fa'],
    gn: ['es'],
    gsw: ['de'],
    hif: ['hif-latn'],
    hsb: ['de'],
    ht: ['fr'],
    ii: ['zh-cn', 'zh-hans'],
    inh: ['ru'],
    iu: ['ike-cans'],
    jut: ['da'],
    jv: ['id'],
    kaa: ['kk-latn', 'kk-cyrl'],
    kbd: ['kbd-cyrl'],
    khw: ['ur'],
    kiu: ['tr'],
    kk: ['kk-cyrl'],
    'kk-arab': ['kk-cyrl'],
    'kk-latn': ['kk-cyrl'],
    'kk-cn': ['kk-arab', 'kk-cyrl'],
    'kk-kz': ['kk-cyrl'],
    'kk-tr': ['kk-latn', 'kk-cyrl'],
    kl: ['da'],
    'ko-kp': ['ko'],
    koi: ['ru'],
    krc: ['ru'],
    ks: ['ks-arab'],
    ksh: ['de'],
    ku: ['ku-latn'],
    'ku-arab': ['ckb'],
    kv: ['ru'],
    lad: ['es'],
    lb: ['de'],
    lbe: ['ru'],
    lez: ['ru'],
    li: ['nl'],
    lij: ['it'],
    liv: ['et'],
    lmo: ['it'],
    ln: ['fr'],
    ltg: ['lv'],
    lzz: ['tr'],
    mai: ['hi'],
    'map-bms': ['jv', 'id'],
    mg: ['fr'],
    mhr: ['ru'],
    min: ['id'],
    mo: ['ro'],
    mrj: ['ru'],
    mwl: ['pt'],
    myv: ['ru'],
    mzn: ['fa'],
    nah: ['es'],
    nap: ['it'],
    nds: ['de'],
    'nds-nl': ['nl'],
    'nl-informal': ['nl'],
    no: ['nb'],
    os: ['ru'],
    pcd: ['fr'],
    pdc: ['de'],
    pdt: ['de'],
    pfl: ['de'],
    pms: ['it'],
    pt: ['pt-br'],
    'pt-br': ['pt'],
    qu: ['es'],
    qug: ['qu', 'es'],
    rgn: ['it'],
    rmy: ['ro'],
    'roa-rup': ['rup'],
    rue: ['uk', 'ru'],
    ruq: ['ruq-latn', 'ro'],
    'ruq-cyrl': ['mk'],
    'ruq-latn': ['ro'],
    sa: ['hi'],
    sah: ['ru'],
    scn: ['it'],
    sg: ['fr'],
    sgs: ['lt'],
    sli: ['de'],
    sr: ['sr-ec'],
    srn: ['nl'],
    stq: ['de'],
    su: ['id'],
    szl: ['pl'],
    tcy: ['kn'],
    tg: ['tg-cyrl'],
    tt: ['tt-cyrl', 'ru'],
    'tt-cyrl': ['ru'],
    ty: ['fr'],
    udm: ['ru'],
    ug: ['ug-arab'],
    uk: ['ru'],
    vec: ['it'],
    vep: ['et'],
    vls: ['nl'],
    vmf: ['de'],
    vot: ['fi'],
    vro: ['et'],
    wa: ['fr'],
    wo: ['fr'],
    wuu: ['zh-hans'],
    xal: ['ru'],
    xmf: ['ka'],
    yi: ['he'],
    za: ['zh-hans'],
    zea: ['nl'],
    zh: ['zh-hans'],
    'zh-classical': ['lzh'],
    'zh-cn': ['zh-hans'],
    'zh-hant': ['zh-hans'],
    'zh-hk': ['zh-hant', 'zh-hans'],
    'zh-min-nan': ['nan'],
    'zh-mo': ['zh-hk', 'zh-hant', 'zh-hans'],
    'zh-my': ['zh-sg', 'zh-hans'],
    'zh-sg': ['zh-hans'],
    'zh-tw': ['zh-hant', 'zh-hans'],
    'zh-yue': ['yue']
  });
})(jQuery);
/* global pluralRuleParser */


(function ($) {
  'use strict'; // jscs:disable

  var language = {
    // CLDR plural rules generated using
    // libs/CLDRPluralRuleParser/tools/PluralXML2JSON.html
    pluralRules: {
      ak: {
        one: 'n = 0..1'
      },
      am: {
        one: 'i = 0 or n = 1'
      },
      ar: {
        zero: 'n = 0',
        one: 'n = 1',
        two: 'n = 2',
        few: 'n % 100 = 3..10',
        many: 'n % 100 = 11..99'
      },
      ars: {
        zero: 'n = 0',
        one: 'n = 1',
        two: 'n = 2',
        few: 'n % 100 = 3..10',
        many: 'n % 100 = 11..99'
      },
      as: {
        one: 'i = 0 or n = 1'
      },
      be: {
        one: 'n % 10 = 1 and n % 100 != 11',
        few: 'n % 10 = 2..4 and n % 100 != 12..14',
        many: 'n % 10 = 0 or n % 10 = 5..9 or n % 100 = 11..14'
      },
      bh: {
        one: 'n = 0..1'
      },
      bn: {
        one: 'i = 0 or n = 1'
      },
      br: {
        one: 'n % 10 = 1 and n % 100 != 11,71,91',
        two: 'n % 10 = 2 and n % 100 != 12,72,92',
        few: 'n % 10 = 3..4,9 and n % 100 != 10..19,70..79,90..99',
        many: 'n != 0 and n % 1000000 = 0'
      },
      bs: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11 or f % 10 = 1 and f % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14 or f % 10 = 2..4 and f % 100 != 12..14'
      },
      cs: {
        one: 'i = 1 and v = 0',
        few: 'i = 2..4 and v = 0',
        many: 'v != 0'
      },
      cy: {
        zero: 'n = 0',
        one: 'n = 1',
        two: 'n = 2',
        few: 'n = 3',
        many: 'n = 6'
      },
      da: {
        one: 'n = 1 or t != 0 and i = 0,1'
      },
      dsb: {
        one: 'v = 0 and i % 100 = 1 or f % 100 = 1',
        two: 'v = 0 and i % 100 = 2 or f % 100 = 2',
        few: 'v = 0 and i % 100 = 3..4 or f % 100 = 3..4'
      },
      fa: {
        one: 'i = 0 or n = 1'
      },
      ff: {
        one: 'i = 0,1'
      },
      fil: {
        one: 'v = 0 and i = 1,2,3 or v = 0 and i % 10 != 4,6,9 or v != 0 and f % 10 != 4,6,9'
      },
      fr: {
        one: 'i = 0,1'
      },
      ga: {
        one: 'n = 1',
        two: 'n = 2',
        few: 'n = 3..6',
        many: 'n = 7..10'
      },
      gd: {
        one: 'n = 1,11',
        two: 'n = 2,12',
        few: 'n = 3..10,13..19'
      },
      gu: {
        one: 'i = 0 or n = 1'
      },
      guw: {
        one: 'n = 0..1'
      },
      gv: {
        one: 'v = 0 and i % 10 = 1',
        two: 'v = 0 and i % 10 = 2',
        few: 'v = 0 and i % 100 = 0,20,40,60,80',
        many: 'v != 0'
      },
      he: {
        one: 'i = 1 and v = 0',
        two: 'i = 2 and v = 0',
        many: 'v = 0 and n != 0..10 and n % 10 = 0'
      },
      hi: {
        one: 'i = 0 or n = 1'
      },
      hr: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11 or f % 10 = 1 and f % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14 or f % 10 = 2..4 and f % 100 != 12..14'
      },
      hsb: {
        one: 'v = 0 and i % 100 = 1 or f % 100 = 1',
        two: 'v = 0 and i % 100 = 2 or f % 100 = 2',
        few: 'v = 0 and i % 100 = 3..4 or f % 100 = 3..4'
      },
      hy: {
        one: 'i = 0,1'
      },
      is: {
        one: 't = 0 and i % 10 = 1 and i % 100 != 11 or t != 0'
      },
      iu: {
        one: 'n = 1',
        two: 'n = 2'
      },
      iw: {
        one: 'i = 1 and v = 0',
        two: 'i = 2 and v = 0',
        many: 'v = 0 and n != 0..10 and n % 10 = 0'
      },
      kab: {
        one: 'i = 0,1'
      },
      kn: {
        one: 'i = 0 or n = 1'
      },
      kw: {
        one: 'n = 1',
        two: 'n = 2'
      },
      lag: {
        zero: 'n = 0',
        one: 'i = 0,1 and n != 0'
      },
      ln: {
        one: 'n = 0..1'
      },
      lt: {
        one: 'n % 10 = 1 and n % 100 != 11..19',
        few: 'n % 10 = 2..9 and n % 100 != 11..19',
        many: 'f != 0'
      },
      lv: {
        zero: 'n % 10 = 0 or n % 100 = 11..19 or v = 2 and f % 100 = 11..19',
        one: 'n % 10 = 1 and n % 100 != 11 or v = 2 and f % 10 = 1 and f % 100 != 11 or v != 2 and f % 10 = 1'
      },
      mg: {
        one: 'n = 0..1'
      },
      mk: {
        one: 'v = 0 and i % 10 = 1 or f % 10 = 1'
      },
      mo: {
        one: 'i = 1 and v = 0',
        few: 'v != 0 or n = 0 or n != 1 and n % 100 = 1..19'
      },
      mr: {
        one: 'i = 0 or n = 1'
      },
      mt: {
        one: 'n = 1',
        few: 'n = 0 or n % 100 = 2..10',
        many: 'n % 100 = 11..19'
      },
      naq: {
        one: 'n = 1',
        two: 'n = 2'
      },
      nso: {
        one: 'n = 0..1'
      },
      pa: {
        one: 'n = 0..1'
      },
      pl: {
        one: 'i = 1 and v = 0',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14',
        many: 'v = 0 and i != 1 and i % 10 = 0..1 or v = 0 and i % 10 = 5..9 or v = 0 and i % 100 = 12..14'
      },
      prg: {
        zero: 'n % 10 = 0 or n % 100 = 11..19 or v = 2 and f % 100 = 11..19',
        one: 'n % 10 = 1 and n % 100 != 11 or v = 2 and f % 10 = 1 and f % 100 != 11 or v != 2 and f % 10 = 1'
      },
      pt: {
        one: 'i = 0..1'
      },
      ro: {
        one: 'i = 1 and v = 0',
        few: 'v != 0 or n = 0 or n != 1 and n % 100 = 1..19'
      },
      ru: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14',
        many: 'v = 0 and i % 10 = 0 or v = 0 and i % 10 = 5..9 or v = 0 and i % 100 = 11..14'
      },
      se: {
        one: 'n = 1',
        two: 'n = 2'
      },
      sh: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11 or f % 10 = 1 and f % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14 or f % 10 = 2..4 and f % 100 != 12..14'
      },
      shi: {
        one: 'i = 0 or n = 1',
        few: 'n = 2..10'
      },
      si: {
        one: 'n = 0,1 or i = 0 and f = 1'
      },
      sk: {
        one: 'i = 1 and v = 0',
        few: 'i = 2..4 and v = 0',
        many: 'v != 0'
      },
      sl: {
        one: 'v = 0 and i % 100 = 1',
        two: 'v = 0 and i % 100 = 2',
        few: 'v = 0 and i % 100 = 3..4 or v != 0'
      },
      sma: {
        one: 'n = 1',
        two: 'n = 2'
      },
      smi: {
        one: 'n = 1',
        two: 'n = 2'
      },
      smj: {
        one: 'n = 1',
        two: 'n = 2'
      },
      smn: {
        one: 'n = 1',
        two: 'n = 2'
      },
      sms: {
        one: 'n = 1',
        two: 'n = 2'
      },
      sr: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11 or f % 10 = 1 and f % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14 or f % 10 = 2..4 and f % 100 != 12..14'
      },
      ti: {
        one: 'n = 0..1'
      },
      tl: {
        one: 'v = 0 and i = 1,2,3 or v = 0 and i % 10 != 4,6,9 or v != 0 and f % 10 != 4,6,9'
      },
      tzm: {
        one: 'n = 0..1 or n = 11..99'
      },
      uk: {
        one: 'v = 0 and i % 10 = 1 and i % 100 != 11',
        few: 'v = 0 and i % 10 = 2..4 and i % 100 != 12..14',
        many: 'v = 0 and i % 10 = 0 or v = 0 and i % 10 = 5..9 or v = 0 and i % 100 = 11..14'
      },
      wa: {
        one: 'n = 0..1'
      },
      zu: {
        one: 'i = 0 or n = 1'
      }
    },
    // jscs:enable

    /**
     * Plural form transformations, needed for some languages.
     *
     * @param {integer} count
     *            Non-localized quantifier
     * @param {Array} forms
     *            List of plural forms
     * @return {string} Correct form for quantifier in this language
     */
    convertPlural: function convertPlural(count, forms) {
      var pluralRules,
          pluralFormIndex,
          index,
          explicitPluralPattern = new RegExp('\\d+=', 'i'),
          formCount,
          form;

      if (!forms || forms.length === 0) {
        return '';
      } // Handle for Explicit 0= & 1= values


      for (index = 0; index < forms.length; index++) {
        form = forms[index];

        if (explicitPluralPattern.test(form)) {
          formCount = parseInt(form.slice(0, form.indexOf('=')), 10);

          if (formCount === count) {
            return form.slice(form.indexOf('=') + 1);
          }

          forms[index] = undefined;
        }
      }

      forms = $.map(forms, function (form) {
        if (form !== undefined) {
          return form;
        }
      });
      pluralRules = this.pluralRules[$.i18n().locale];

      if (!pluralRules) {
        // default fallback.
        return count === 1 ? forms[0] : forms[1];
      }

      pluralFormIndex = this.getPluralForm(count, pluralRules);
      pluralFormIndex = Math.min(pluralFormIndex, forms.length - 1);
      return forms[pluralFormIndex];
    },

    /**
     * For the number, get the plural for index
     *
     * @param {integer} number
     * @param {Object} pluralRules
     * @return {integer} plural form index
     */
    getPluralForm: function getPluralForm(number, pluralRules) {
      var i,
          pluralForms = ['zero', 'one', 'two', 'few', 'many', 'other'],
          pluralFormIndex = 0;

      for (i = 0; i < pluralForms.length; i++) {
        if (pluralRules[pluralForms[i]]) {
          if (pluralRuleParser(pluralRules[pluralForms[i]], number)) {
            return pluralFormIndex;
          }

          pluralFormIndex++;
        }
      }

      return pluralFormIndex;
    },

    /**
     * Converts a number using digitTransformTable.
     *
     * @param {number} num Value to be converted
     * @param {boolean} integer Convert the return value to an integer
     * @return {string} The number converted into a String.
     */
    convertNumber: function convertNumber(num, integer) {
      var tmp, item, i, transformTable, numberString, convertedNumber; // Set the target Transform table:

      transformTable = this.digitTransformTable($.i18n().locale);
      numberString = String(num);
      convertedNumber = '';

      if (!transformTable) {
        return num;
      } // Check if the restore to Latin number flag is set:


      if (integer) {
        if (parseFloat(num, 10) === num) {
          return num;
        }

        tmp = [];

        for (item in transformTable) {
          tmp[transformTable[item]] = item;
        }

        transformTable = tmp;
      }

      for (i = 0; i < numberString.length; i++) {
        if (transformTable[numberString[i]]) {
          convertedNumber += transformTable[numberString[i]];
        } else {
          convertedNumber += numberString[i];
        }
      }

      return integer ? parseFloat(convertedNumber, 10) : convertedNumber;
    },

    /**
     * Grammatical transformations, needed for inflected languages.
     * Invoked by putting {{grammar:form|word}} in a message.
     * Override this method for languages that need special grammar rules
     * applied dynamically.
     *
     * @param {string} word
     * @param {string} form
     * @return {string}
     */
    // eslint-disable-next-line no-unused-vars
    convertGrammar: function convertGrammar(word, form) {
      return word;
    },

    /**
     * Provides an alternative text depending on specified gender. Usage
     * {{gender:[gender|user object]|masculine|feminine|neutral}}. If second
     * or third parameter are not specified, masculine is used.
     *
     * These details may be overriden per language.
     *
     * @param {string} gender
     *      male, female, or anything else for neutral.
     * @param {Array} forms
     *      List of gender forms
     *
     * @return {string}
     */
    gender: function gender(_gender, forms) {
      if (!forms || forms.length === 0) {
        return '';
      }

      while (forms.length < 2) {
        forms.push(forms[forms.length - 1]);
      }

      if (_gender === 'male') {
        return forms[0];
      }

      if (_gender === 'female') {
        return forms[1];
      }

      return forms.length === 3 ? forms[2] : forms[0];
    },

    /**
     * Get the digit transform table for the given language
     * See http://cldr.unicode.org/translation/numbering-systems
     *
     * @param {string} language
     * @return {Array|boolean} List of digits in the passed language or false
     * representation, or boolean false if there is no information.
     */
    digitTransformTable: function digitTransformTable(language) {
      var tables = {
        ar: '٠١٢٣٤٥٦٧٨٩',
        fa: '۰۱۲۳۴۵۶۷۸۹',
        ml: '൦൧൨൩൪൫൬൭൮൯',
        kn: '೦೧೨೩೪೫೬೭೮೯',
        lo: '໐໑໒໓໔໕໖໗໘໙',
        or: '୦୧୨୩୪୫୬୭୮୯',
        kh: '០១២៣៤៥៦៧៨៩',
        pa: '੦੧੨੩੪੫੬੭੮੯',
        gu: '૦૧૨૩૪૫૬૭૮૯',
        hi: '०१२३४५६७८९',
        my: '၀၁၂၃၄၅၆၇၈၉',
        ta: '௦௧௨௩௪௫௬௭௮௯',
        te: '౦౧౨౩౪౫౬౭౮౯',
        th: '๐๑๒๓๔๕๖๗๘๙',
        // FIXME use iso 639 codes
        bo: '༠༡༢༣༤༥༦༧༨༩' // FIXME use iso 639 codes

      };

      if (!tables[language]) {
        return false;
      }

      return tables[language].split('');
    }
  };
  $.extend($.i18n.languages, {
    'default': language
  });
})(jQuery);
/*!
 * jQuery Internationalization library - Message Store
 *
 * Copyright (C) 2012 Santhosh Thottingal
 *
 * jquery.i18n is dual licensed GPLv2 or later and MIT. You don't have to do anything special to
 * choose one license or the other and you don't have to notify anyone which license you are using.
 * You are free to use UniversalLanguageSelector in commercial projects as long as the copyright
 * header is left intact. See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */


(function ($) {
  'use strict';

  var MessageStore = function MessageStore() {
    this.messages = {};
    this.sources = {};
  };

  function jsonMessageLoader(url) {
    var deferred = $.Deferred();
    $.getJSON(url).done(deferred.resolve).fail(function (jqxhr, settings, exception) {
      $.i18n.log('Error in loading messages from ' + url + ' Exception: ' + exception); // Ignore 404 exception, because we are handling fallabacks explicitly

      deferred.resolve();
    });
    return deferred.promise();
  }
  /**
   * See https://github.com/wikimedia/jquery.i18n/wiki/Specification#wiki-Message_File_Loading
   */


  MessageStore.prototype = {
    /**
     * General message loading API This can take a URL string for
     * the json formatted messages.
     * <code>load('path/to/all_localizations.json');</code>
     *
     * This can also load a localization file for a locale <code>
     * load( 'path/to/de-messages.json', 'de' );
     * </code>
     * A data object containing message key- message translation mappings
     * can also be passed Eg:
     * <code>
     * load( { 'hello' : 'Hello' }, optionalLocale );
     * </code> If the data argument is
     * null/undefined/false,
     * all cached messages for the i18n instance will get reset.
     *
     * @param {string|Object} source
     * @param {string} locale Language tag
     * @return {jQuery.Promise}
     */
    load: function load(source, locale) {
      var key = null,
          deferred = null,
          deferreds = [],
          messageStore = this;

      if (typeof source === 'string') {
        // This is a URL to the messages file.
        $.i18n.log('Loading messages from: ' + source);
        deferred = jsonMessageLoader(source).done(function (localization) {
          messageStore.set(locale, localization);
        });
        return deferred.promise();
      }

      if (locale) {
        // source is an key-value pair of messages for given locale
        messageStore.set(locale, source);
        return $.Deferred().resolve();
      } else {
        // source is a key-value pair of locales and their source
        for (key in source) {
          if (Object.prototype.hasOwnProperty.call(source, key)) {
            locale = key; // No {locale} given, assume data is a group of languages,
            // call this function again for each language.

            deferreds.push(messageStore.load(source[key], locale));
          }
        }

        return $.when.apply($, deferreds);
      }
    },

    /**
     * Set messages to the given locale.
     * If locale exists, add messages to the locale.
     *
     * @param {string} locale
     * @param {Object} messages
     */
    set: function set(locale, messages) {
      if (!this.messages[locale]) {
        this.messages[locale] = messages;
      } else {
        this.messages[locale] = $.extend(this.messages[locale], messages);
      }
    },

    /**
     *
     * @param {string} locale
     * @param {string} messageKey
     * @return {boolean}
     */
    get: function get(locale, messageKey) {
      return this.messages[locale] && this.messages[locale][messageKey];
    }
  };
  $.extend($.i18n.messageStore, new MessageStore());
})(jQuery);
/*!
 * jQuery Internationalization library
 *
 * Copyright (C) 2011-2013 Santhosh Thottingal, Neil Kandalgaonkar
 *
 * jquery.i18n is dual licensed GPLv2 or later and MIT. You don't have to do
 * anything special to choose one license or the other and you don't have to
 * notify anyone which license you are using. You are free to use
 * UniversalLanguageSelector in commercial projects as long as the copyright
 * header is left intact. See files GPL-LICENSE and MIT-LICENSE for details.
 *
 * @licence GNU General Public Licence 2.0 or later
 * @licence MIT License
 */


(function ($) {
  'use strict';

  var MessageParser = function MessageParser(options) {
    this.options = $.extend({}, $.i18n.parser.defaults, options);
    this.language = $.i18n.languages[String.locale] || $.i18n.languages['default'];
    this.emitter = $.i18n.parser.emitter;
  };

  MessageParser.prototype = {
    constructor: MessageParser,
    simpleParse: function simpleParse(message, parameters) {
      return message.replace(/\$(\d+)/g, function (str, match) {
        var index = parseInt(match, 10) - 1;
        return parameters[index] !== undefined ? parameters[index] : '$' + match;
      });
    },
    parse: function parse(message, replacements) {
      if (message.indexOf('{{') < 0) {
        return this.simpleParse(message, replacements);
      }

      this.emitter.language = $.i18n.languages[$.i18n().locale] || $.i18n.languages['default'];
      return this.emitter.emit(this.ast(message), replacements);
    },
    ast: function ast(message) {
      var pipe,
          colon,
          backslash,
          anyCharacter,
          dollar,
          digits,
          regularLiteral,
          regularLiteralWithoutBar,
          regularLiteralWithoutSpace,
          escapedOrLiteralWithoutBar,
          escapedOrRegularLiteral,
          templateContents,
          templateName,
          openTemplate,
          closeTemplate,
          expression,
          paramExpression,
          result,
          pos = 0; // Try parsers until one works, if none work return null

      function choice(parserSyntax) {
        return function () {
          var i, result;

          for (i = 0; i < parserSyntax.length; i++) {
            result = parserSyntax[i]();

            if (result !== null) {
              return result;
            }
          }

          return null;
        };
      } // Try several parserSyntax-es in a row.
      // All must succeed; otherwise, return null.
      // This is the only eager one.


      function sequence(parserSyntax) {
        var i,
            res,
            originalPos = pos,
            result = [];

        for (i = 0; i < parserSyntax.length; i++) {
          res = parserSyntax[i]();

          if (res === null) {
            pos = originalPos;
            return null;
          }

          result.push(res);
        }

        return result;
      } // Run the same parser over and over until it fails.
      // Must succeed a minimum of n times; otherwise, return null.


      function nOrMore(n, p) {
        return function () {
          var originalPos = pos,
              result = [],
              parsed = p();

          while (parsed !== null) {
            result.push(parsed);
            parsed = p();
          }

          if (result.length < n) {
            pos = originalPos;
            return null;
          }

          return result;
        };
      } // Helpers -- just make parserSyntax out of simpler JS builtin types


      function makeStringParser(s) {
        var len = s.length;
        return function () {
          var result = null;

          if (message.slice(pos, pos + len) === s) {
            result = s;
            pos += len;
          }

          return result;
        };
      }

      function makeRegexParser(regex) {
        return function () {
          var matches = message.slice(pos).match(regex);

          if (matches === null) {
            return null;
          }

          pos += matches[0].length;
          return matches[0];
        };
      }

      pipe = makeStringParser('|');
      colon = makeStringParser(':');
      backslash = makeStringParser('\\');
      anyCharacter = makeRegexParser(/^./);
      dollar = makeStringParser('$');
      digits = makeRegexParser(/^\d+/);
      regularLiteral = makeRegexParser(/^[^{}[\]$\\]/);
      regularLiteralWithoutBar = makeRegexParser(/^[^{}[\]$\\|]/);
      regularLiteralWithoutSpace = makeRegexParser(/^[^{}[\]$\s]/); // There is a general pattern:
      // parse a thing;
      // if it worked, apply transform,
      // otherwise return null.
      // But using this as a combinator seems to cause problems
      // when combined with nOrMore().
      // May be some scoping issue.

      function transform(p, fn) {
        return function () {
          var result = p();
          return result === null ? null : fn(result);
        };
      } // Used to define "literals" within template parameters. The pipe
      // character is the parameter delimeter, so by default
      // it is not a literal in the parameter


      function literalWithoutBar() {
        var result = nOrMore(1, escapedOrLiteralWithoutBar)();
        return result === null ? null : result.join('');
      }

      function literal() {
        var result = nOrMore(1, escapedOrRegularLiteral)();
        return result === null ? null : result.join('');
      }

      function escapedLiteral() {
        var result = sequence([backslash, anyCharacter]);
        return result === null ? null : result[1];
      }

      choice([escapedLiteral, regularLiteralWithoutSpace]);
      escapedOrLiteralWithoutBar = choice([escapedLiteral, regularLiteralWithoutBar]);
      escapedOrRegularLiteral = choice([escapedLiteral, regularLiteral]);

      function replacement() {
        var result = sequence([dollar, digits]);

        if (result === null) {
          return null;
        }

        return ['REPLACE', parseInt(result[1], 10) - 1];
      }

      templateName = transform( // see $wgLegalTitleChars
      // not allowing : due to the need to catch "PLURAL:$1"
      makeRegexParser(/^[ !"$&'()*,./0-9;=?@A-Z^_`a-z~\x80-\xFF+-]+/), function (result) {
        return result.toString();
      });

      function templateParam() {
        var expr,
            result = sequence([pipe, nOrMore(0, paramExpression)]);

        if (result === null) {
          return null;
        }

        expr = result[1]; // use a "CONCAT" operator if there are multiple nodes,
        // otherwise return the first node, raw.

        return expr.length > 1 ? ['CONCAT'].concat(expr) : expr[0];
      }

      function templateWithReplacement() {
        var result = sequence([templateName, colon, replacement]);
        return result === null ? null : [result[0], result[2]];
      }

      function templateWithOutReplacement() {
        var result = sequence([templateName, colon, paramExpression]);
        return result === null ? null : [result[0], result[2]];
      }

      templateContents = choice([function () {
        var res = sequence([// templates can have placeholders for dynamic
        // replacement eg: {{PLURAL:$1|one car|$1 cars}}
        // or no placeholders eg:
        // {{GRAMMAR:genitive|{{SITENAME}}}
        choice([templateWithReplacement, templateWithOutReplacement]), nOrMore(0, templateParam)]);
        return res === null ? null : res[0].concat(res[1]);
      }, function () {
        var res = sequence([templateName, nOrMore(0, templateParam)]);

        if (res === null) {
          return null;
        }

        return [res[0]].concat(res[1]);
      }]);
      openTemplate = makeStringParser('{{');
      closeTemplate = makeStringParser('}}');

      function template() {
        var result = sequence([openTemplate, templateContents, closeTemplate]);
        return result === null ? null : result[1];
      }

      expression = choice([template, replacement, literal]);
      paramExpression = choice([template, replacement, literalWithoutBar]);

      function start() {
        var result = nOrMore(0, expression)();

        if (result === null) {
          return null;
        }

        return ['CONCAT'].concat(result);
      }

      result = start();
      /*
       * For success, the pos must have gotten to the end of the input
       * and returned a non-null.
       * n.b. This is part of language infrastructure, so we do not throw an
       * internationalizable message.
       */

      if (result === null || pos !== message.length) {
        throw new Error('Parse error at position ' + pos.toString() + ' in input: ' + message);
      }

      return result;
    }
  };
  $.extend($.i18n.parser, new MessageParser());
})(jQuery);