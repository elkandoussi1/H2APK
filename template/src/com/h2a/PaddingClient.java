package com.h2a;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.HashMap;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
public class PaddingClient extends WebViewClient {
  public interface PullCallback {
    void onPageFinished();
  }
  private PullCallback callback;
  private HashSet<String> blocked;
  private boolean blockAds;
  private boolean useAdGuardDNS;
  private HashMap<String,String> dnsCache;
  private int dnsSeq;
  private boolean useAssetLoader = Cfg.assetLoader;
  public PaddingClient() { this.callback = null; this.blockAds = false; this.useAdGuardDNS = false; }
  public PaddingClient(PullCallback cb) { this.callback = cb; this.blockAds = false; this.useAdGuardDNS = false; }
  public PaddingClient(boolean block) { this.callback = null; init(block, false); }
  public PaddingClient(boolean block, boolean dns) { this.callback = null; init(block, dns); }
  public PaddingClient(PullCallback cb, boolean block) { this.callback = cb; init(block, false); }
  public PaddingClient(PullCallback cb, boolean block, boolean dns) { this.callback = cb; init(block, dns); }
  private void init(boolean block, boolean dns) {
    this.blockAds = block;
    this.useAdGuardDNS = dns;
    if (dns) { dnsCache = new HashMap<String,String>(); dnsSeq = 0; }
    if (!block) return;
    blocked = new HashSet<String>();
    String[] base = {
      "doubleclick.net","googlesyndication.com","googleadservices.com","adservice.google.com",
      "amazon-adsystem.com","adsrvr.org","adnxs.com","openx.net","pubmatic.com",
      "rubiconproject.com","criteo.com","casalemedia.com","adform.net","appnexus.com",
      "bidswitch.net","moatads.com","taboola.com","outbrain.com","popads.net",
      "propellerads.com","exoclick.com","juicyads.com","advertising.com","adzerk.net",
      "criteo.net","sharethrough.com","triplelift.com","sovrn.com","indexww.com",
      "contextweb.com","rlcdn.com","adsafeprotected.com","1rx.io","adlightning.com",
      "qerelink.qpon","propellerpops.com","onclickads.net","onclkds.com","popcash.net",
      "trafficjunky.net","adsterra.com","ad-maven.com","adinplay.com","monetag.com",
      "prop-fra-01.com","evadav.net","galaksion.com"
    };
    for (String d : base) blocked.add(d);
    String[] redirects = {
      "disgusting-sun.com","disgusting-zoo.com","disgusting-moon.com",
      "tsyndolls.com","tsyndoll.com","trafficjunky.com","trafficjunky.net",
      "tsyndicate.com","rtbsuperhub.com","bidresolving.com","pushzones.com",
      "adsafeprotected.com","rta.direct","wwwpromoter.com","theankara.com",
      "chrunched.com","evadav.net","straightdirectory.com","simplecyberdefense.com",
      "clickaine.com","clickadu.com","g-em.com","adxxx.com","xvideoslive.com",
      "rm358.com"
    };
    for (String d : redirects) blocked.add(d);
    String[] adg = {
      "2mdn.net","2o7.net","33across.com","4cp776.site","4dex.io","abmr.net",
      "addthis.com","adengage.com","adf.ly","adkeeper.net","admedo.com",
      "adnetwork.net","adobela.com","adonnetwork.com","adplexo.com",
      "adpone.com","adpushup.com","adreclaim.com","adrecover.com",
      "adservd.com","adservicer.com","adspirit.com","adsymptotic.com","adtaily.com",
      "adtech.com","adtelligent.com","adtrue.com","adups.com","advangelists.com",
      "adventori.com","adversal.com","advertnative.com","adview.com","adzerk.com",
      "affexa.com","affiliaweb.com","affluentco.com","airpush.com",
      "amobee.com","ampliffy.com","aniview.com","antevenio.com",
      "aolp.jp","apester.com","appenda.com","arcadebuzz.com","atdmt.com","atlassolutions.com",
      "audiencetv.com","avantisvideo.com","bannerflow.com","bannersnack.com",
      "baronsoffers.com","beachfront.com","beintoo.com","bet365affiliates.com","bf-ad.net",
      "bidder.com","bidgear.com","bidmachine.io","bizrate.com","blismedia.com","blogads.com",
      "bluecava.com","bluekai.com","bounceexchange.com","brainty.com","brightcom.com",
      "btrll.com","buysellads.com","buzzvil.com","carbonads.com","carambo.la","cbox.ws",
      "celtra.com","cetzboo.com","chango.com","cheqzone.com","chitika.com","choicestream.com",
      "clean.gg","clearseasmedia.com","clevertap.com","clixgalore.com","cmail1.com",
      "coinad.com","collective.com","commindo-media.de","commumobi.com","compasslabs.ai",
      "congstar.de","connatix.com","connexity.net","consumable.com","conversantmedia.com",
      "creafi.com","crispmedia.com","cxense.com","cyberagent.co.jp","dable.io","dainikb.com",
      "datawrkz.com","dc-storm.com","decenterads.com","deloton.com","deltaprojects.com",
      "demdex.com","dep-x.com","dgmax.io","digitaltarget.ru","dpcdn.com",
      "e-planning.net","effectivemeasure.com","eleavers.com","emxdigital.com","engagebdr.com",
      "enoratraffic.com","epom.com","eskimi.com","etargetnet.com","everesttech.net",
      "exosrv.com","exponential.com","eyeota.net","eyereturn.com","fastcmp.com",
      "fearlessrevenue.com","flixsyndication.com","flocktory.com","freestar.com",
      "fuseplatform.com","gamoshi.io","genieesspv.jp","getintent.com","gigya.com",
      "gladlyads.com","gladlyads.in","globalhopedall.com","globulematchw.xyz","gobicybe.com",
      "gravity.com","greedseed.com","grofers.com","grow-ist.com",
      "growthhouse.co","gumgum.com","hearty.llc","hellobar.com","hiido.com","hilltopads.com",
      "hola-player.com","hoodline.com","hyprmx.com","iasds01.com","ibillboard.com",
      "ictv.com","idle-ads.com","ignitionone.com","impact.com","imrworldwide.com",
      "infolinks.com","inmobi.com","innity.com","intentiq.com","intergi.com","inviziads.com",
      "iocket.net","ipredictive.com","ispot.tv","jampp.com","jivox.com","kadam.net",
      "kevel.com","keymedia.info","kixer.com","komoona.com","krux.net","lacreates.com",
      "leadbolt.net","leadklozer.com","lemmatechnologies.com",
      "ligatus.com","linkprice.com","linuxmobi.com","liquidm.com","lkqd.com","lognv.com",
      "longtailvideo.com","loopme.com","lucky-ads.com","macromill.com","magnite.com",
      "mailfire.io","mantisad.net","marchex.io","markethealth.com","marketron.com",
      "marvellousmachine.com","masteraffiliates.org","mathtag.com","mb104.com",
      "mc-market.org","mcsqd.com","media.net","media6degrees.com","mediaalley.com",
      "mediabong.net","medialand.ru","medianetnow.com","mediasquare.fr","medicx.com",
      "merchenta.com","mgage.com","microad.jp","millennialmedia.com","misterbell.it",
      "mixmarket.biz","mmismm.com","mobads.baidu.com","mobivity.com","mobtrks.com",
      "mopub.com","mowaymedia.com","musculahq.com","myaffiliates.com","myexpertise.de",
      "mynativeplatform.com","narrative.io","nativeads.com","networld.hk","newstogram.com",
      "nexac.com","ngenix.net","nielsen-online.com","nitroscripts.com","nowspots.com",
      "nxtck.com","ogury.com","omniture.com","onads.com","onaudience.com","onedmp.com",
      "onetag-sys.com","openmarket.mobi","optmnstr.com","oraclecloudads.com",
      "padsdel.com","pagefair.net","parrable.com","payclick.it",
      "pcash.im","peerfly.com","performancerevenues.com","permutive.com","phoenix-widget.com",
      "pixfuture.com","popin.cc","powerlinks.com","premiumnetwork.com",
      "primis.tech","projectagora.com","provers.pro","pubgalaxy.com",
      "pubnative.net","pulseem.com","pusherism.com","pushhouse.com",
      "quantcount.com","quantum-ad-s.com","quantserve.com","qubit.com","quinstreet.com",
      "r-ad.ne.jp","radiumone.com","rankmylist.com","rayjump.com","reachforce.com",
      "redshell.io","redirect.com","refersion.com","reklama.com","reklamstore.com",
      "remintrex.com","reso.no","resultrix.com","retargetly.com","revenuehut.com","revup.jp",
      "rfihub.com","rhythmone.com","richaudience.com","ringsget.com","roia.biz",
      "rtbhouse.com","rtbsystem.org","ru4.com","s4m.io","scorecardresearch.com",
      "scribblelive.com","seeding-ads.com","segment.io","sekindo.com",
      "sellwild.com","seniormind.de","seoquake.com","serpwoo.com","sexad.net",
      "shareaholic.com","shareasale.com","sharethis.com","shorte.st","silverpop.com",
      "sitescout.com","skimresources.com","smaato.com","smartadserver.com","smartclip.net",
      "smartyads.com","snapads.com","snigelweb.com","sociallykeeda.com","socialprivacy.org",
      "softcube.com","sonobi.com","soo.gd","sparkflow.ai","spctrm.com","specless.tech",
      "speedcurve.com","spilgames.com","spinmedia.com","spotxchange.com","springserve.com",
      "sputnik-burst.info","stackadapt.com","steelhouse.com","strapad.com","streamrail.net",
      "sublimemedia.net","subusers.com","successfultogether.co.uk","sudoads.com",
      "sundaysky.com","superawesome.tv","supersonicads.com",
      "survata.com","syndopop.com","tabmo.io","taptica.com","teads.tv",
      "technoratimedia.com","telecoming.com","tentaculos.net",
      "theadx.com","theblogfrog.com","thebootube.com","thundertech.com","tibacta.com",
      "tidal.life","tiqcdn.com","tmsmedia.io","tonefuse.com","tradedoubler.com",
      "traffichaus.com","trafficstars.com","trekblue.com",
      "truoptik.com","tubemogul.com","turn.com","twiagos.com","tynt.com","uberads.com",
      "udtwenu.com","ultimamedia.com","unbounce.com","underdogmedia.com","undertone.com",
      "uniconsent.com","unilead.com","unruly.co","upravel.com","vado.tv","valueclickmedia.com",
      "vastserved.com","vertex-int.com","vibrantmedia.com","vidazoo.com","videoamp.com",
      "videobyte.com","vidoomy.com","viglink.com","viral-loops.com","virtusize.com",
      "visiblemeasures.com","viulife.com","voicefive.com","voluum.com","vrtcal.com",
      "vungle.com","wagawin.com","weborama.com",
      "whistleout.com","widgetserve.com","worldnaturenet.xyz","wp113.com",
      "xad.com","xaxis.com","xpanama.net","xplusone.com",
      "yashi.com","yieldivision.com","yieldlab.net","yieldlove.com","yieldmo.com",
      "yieldoptimizer.com","yoc.com","yodle.com","yoggrt.com","z4rtist.com",
      "zap.buzz","zeeto.io","zemanta.com","zeotap.com","zetaglobal.com","ziffdavis.com",
      "zipari.com","zmags.com","zprk.com","zymanga.net","zymanga.com"
    };
    for (String d : adg) blocked.add(d);

  }
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
    return handleNavigation(view, url, false);
  }
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
    boolean gesture = false;
    if (android.os.Build.VERSION.SDK_INT >= 24) gesture = request.hasGesture();
    String url = request.getUrl().toString();
    handleNavigation(view, url, gesture);
    return true;
  }
  private boolean handleNavigation(WebView view, String url, boolean hasGesture) {
    if (!blockAds || url == null) { view.loadUrl(url); return true; }
    String cur = view.getUrl();
    if (cur != null) {
      try {
        java.net.URL nu = new java.net.URL(url);
        java.net.URL cu = new java.net.URL(cur);
        String nh = nu.getHost();
        String ch = cu.getHost();
        if (nh != null && ch != null && (nh.equals(ch) || nh.endsWith("."+ch))) {
          view.loadUrl(url);
          return true;
        }
      } catch (Exception e) {}
    }
    view.stopLoading();
    return true;
  }
  // @deprecated - kept for compilation compatibility, not called
  private boolean isSuspiciousUrl(String url, String currentUrl) {
    try {
      java.net.URL u = new java.net.URL(url);
      java.net.URL cu = new java.net.URL(currentUrl);
      String h = u.getHost();
      String ch = cu.getHost();
      if (h == null || ch == null || h.equals(ch) || h.endsWith("."+ch)) return false;
      String q = u.getQuery();
      String p = u.getPath();
      if (q != null && q.toLowerCase().matches(".*(clickid|zoneid|campaign|sid|subid|aff|offerid|traffic|adid|popunder|popup|redirect|promo|partner|ymid|token).*"))
        return true;
      if (p != null && p.matches("/[0-9]+/[0-9]+"))
        return true;
      if (h.matches("[a-z]{2,3}[0-9]{2,4}\\..*"))
        return true;
    } catch (Exception e) {}
    return false;
  }

  private boolean isAdDomain(String url) {
    if (blocked == null) return false;
    try {
      java.net.URL p = new java.net.URL(url);
      String h = p.getHost();
      if (h != null) {
        while (h.contains(".")) {
          if (blocked.contains(h)) return true;
          h = h.substring(h.indexOf(".") + 1);
        }
        if (blocked.contains(h)) return true;
      }
    } catch (Exception e) {}
    return false;
  }
  private static String guessMime(String path) {
    String l = path.toLowerCase();
    if (l.endsWith(".html") || l.endsWith(".htm")) return "text/html";
    if (l.endsWith(".css")) return "text/css";
    if (l.endsWith(".js")) return "application/javascript";
    if (l.endsWith(".json")) return "application/json";
    if (l.endsWith(".png")) return "image/png";
    if (l.endsWith(".jpg") || l.endsWith(".jpeg")) return "image/jpeg";
    if (l.endsWith(".gif")) return "image/gif";
    if (l.endsWith(".svg")) return "image/svg+xml";
    if (l.endsWith(".webp")) return "image/webp";
    if (l.endsWith(".ico")) return "image/x-icon";
    if (l.endsWith(".woff")) return "font/woff";
    if (l.endsWith(".woff2")) return "font/woff2";
    return "text/plain";
  }
  private WebResourceResponse serveAsset(WebView view, String url) {
    try {
      if (!url.startsWith("file:///android_asset/")) return null;
      String path = url.substring("file:///android_asset/".length());
      if (path == null || path.isEmpty() || "/".equals(path)) path = "/index.html";
      if (path.startsWith("/")) path = path.substring(1);
      if (path.endsWith("/")) {
        path = path + "index.html";
      } else {
        try {
          InputStream test = view.getContext().getAssets().open(path);
          test.close();
        } catch (Exception e) {
          path = path + "/index.html";
        }
      }
      InputStream is = view.getContext().getAssets().open(path);
      return new WebResourceResponse(guessMime(path), null, is);
    } catch (Exception e) {}
    return null;
  }
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
    if (useAssetLoader) { WebResourceResponse a = serveAsset(view, url); if (a != null) return a; }
    return intercept(url);
  }
  @Override
  public WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest req) {
    if (useAssetLoader) { WebResourceResponse a = serveAsset(view, req.getUrl().toString()); if (a != null) return a; }
    return intercept(req.getUrl().toString());
  }
  private String adguardResolve(String host) {
    if (host == null) return null;
    if (dnsCache.containsKey(host)) return dnsCache.get(host);
    try {
      byte[][] parts = new byte[][]{host.getBytes("UTF-8")};
      int qlen = 12 + host.length() + 2 + 4;
      byte[] q = new byte[qlen];
      q[0] = (byte)(dnsSeq >> 8); q[1] = (byte)(dnsSeq & 0xFF); dnsSeq++;
      q[2] = 1; q[5] = 1;
      int pos = 12;
      for (String label : host.split("\\.")) {
        byte[] b = label.getBytes("UTF-8");
        q[pos++] = (byte)b.length;
        System.arraycopy(b, 0, q, pos, b.length);
        pos += b.length;
      }
      q[pos++] = 0; q[pos++] = 1; q[pos++] = 0; q[pos++] = 1;
      DatagramSocket s = new DatagramSocket();
      s.setSoTimeout(2000);
      s.send(new DatagramPacket(q, pos, InetAddress.getByName("94.140.14.14"), 53));
      byte[] r = new byte[512];
      DatagramPacket resp = new DatagramPacket(r, 512);
      s.receive(resp);
      s.close();
      int nans = ((r[6] & 0xFF) << 8) | (r[7] & 0xFF);
      int off = pos;
      for (int i = 0; i < nans; i++) {
        if ((r[off] & 0xC0) == 0xC0) off += 2;
        else { while (r[off] != 0) off += (r[off] & 0xFF) + 1; off++; }
        int type = ((r[off + 1] & 0xFF) << 8) | (r[off + 2] & 0xFF);
        int len = ((r[off + 9] & 0xFF) << 8) | (r[off + 10] & 0xFF);
        if (type == 1 && len == 4) {
          String ip = (r[off+11]&0xFF)+"."+(r[off+12]&0xFF)+"."+(r[off+13]&0xFF)+"."+(r[off+14]&0xFF);
          dnsCache.put(host, ip);
          return ip;
        }
        off += 10 + len;
      }
      dnsCache.put(host, ".");
      return ".";
    } catch (Exception e) {
      dnsCache.put(host, ".");
      return ".";
    }
  }

  private WebResourceResponse intercept(String url) {
    if (blockAds && url != null) {
      String l = url.toLowerCase();
      if (l.startsWith("intent://") || l.startsWith("market://") ||
          l.startsWith("shopee://") || l.startsWith("shopeelink://") || l.startsWith("lazada://"))
        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
      if (blocked != null) {
        try {
          java.net.URL p = new java.net.URL(url);
          String h = p.getHost();
          if (h != null) {
            while (h.contains(".")) {
              if (blocked.contains(h)) return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
              h = h.substring(h.indexOf(".") + 1);
            }
            if (blocked.contains(h)) return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
          }
        } catch (Exception e) {}
      }
      if (useAdGuardDNS) {
        try {
          java.net.URL p = new java.net.URL(url);
          String ip = adguardResolve(p.getHost());
          if ("0.0.0.0".equals(ip))
            return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
        } catch (Exception e) {}
      }
    }
    return null;
  }
  @Override
  public void onPageFinished(WebView view, String url) {
    super.onPageFinished(view, url);
    if (blockAds) {
      view.evaluateJavascript(
        "(function(){" +
        "var _o=window.open;window.open=function(url,n){if(url){try{var a=document.createElement('a');a.href=url;if(a.hostname&&a.hostname!==location.hostname)return null;}catch(e){}}if(n==='_blank'||n==='_new')return null;return _o.apply(this,arguments);};" +
        "if(navigator.sendBeacon)navigator.sendBeacon=function(){return false;};" +
        "try{Object.defineProperty(navigator,'plugins',{get:function(){return[1,2,3,4,5];}})}catch(e){}" +
        "try{Object.defineProperty(navigator,'hardwareConcurrency',{get:function(){return 4;}})}catch(e){}" +
        "try{Object.defineProperty(navigator,'deviceMemory',{get:function(){return 4;}})}catch(e){}" +
        "try{delete window.RTCPeerConnection;window.RTCPeerConnection=undefined;}catch(e){}" +
        "try{delete window.webkitRTCPeerConnection;window.webkitRTCPeerConnection=undefined;}catch(e){}" +
        "var s=document.createElement('style');" +
        "s.textContent='.adsbox,.adsbygoogle," +
        "ins.adsbygoogle,div[id^=div-gpt-ad],div[id^=google_ads_iframe_]," +
        ".ad-popup,.ad-overlay,.modal-ad,.overlay-ad,.popup-ad,.popup-overlay," +
        "[class*=popup-ad],.sponsored-content,[id*=google_ads]{display:none!important}';" +
        "document.head.appendChild(s);" +
        "var adDomains={" +
        "\"doubleclick.net\":1,\"googlesyndication.com\":1,\"googleadservices.com\":1,\"adservice.google.com\":1," +
        "\"amazon-adsystem.com\":1,\"adsrvr.org\":1,\"adnxs.com\":1,\"openx.net\":1,\"pubmatic.com\":1," +
        "\"rubiconproject.com\":1,\"criteo.com\":1,\"casalemedia.com\":1,\"adform.net\":1,\"appnexus.com\":1," +
        "\"bidswitch.net\":1,\"moatads.com\":1,\"taboola.com\":1,\"outbrain.com\":1,\"popads.net\":1," +
        "\"propellerads.com\":1,\"exoclick.com\":1,\"juicyads.com\":1,\"advertising.com\":1,\"adzerk.net\":1," +
        "\"criteo.net\":1,\"sharethrough.com\":1,\"triplelift.com\":1,\"sovrn.com\":1,\"indexww.com\":1," +
        "\"contextweb.com\":1,\"rlcdn.com\":1,\"adsafeprotected.com\":1,\"1rx.io\":1,\"adlightning.com\":1," +
        "\"qerelink.qpon\":1,\"propellerpops.com\":1,\"onclickads.net\":1,\"onclkds.com\":1,\"popcash.net\":1," +
        "\"trafficjunky.net\":1,\"adsterra.com\":1,\"ad-maven.com\":1,\"adinplay.com\":1,\"monetag.com\":1," +
        "\"prop-fra-01.com\":1,\"evadav.net\":1,\"galaksion.com\":1" +
        "};" +
        "function isAdSrc(el){" +
        "var t=el.tagName;" +
        "if(t==='IMG'||t==='IFRAME'||t==='SCRIPT'){" +
        "var s=el.src||el.getAttribute('src')||'';" +
        "for(var d in adDomains){if(s.indexOf(d)!==-1)return true;}" +
        "}" +
        "return false;" +
        "}" +
        "function hideAds(root){" +
        "var imgs=root.querySelectorAll('img,iframe,script');" +
        "for(var i=0;i<imgs.length;i++){" +
        "if(isAdSrc(imgs[i])){" +
        "var p=imgs[i].parentElement;" +
        "for(var j=0;j<4&&p&&p!==document.body;j++){" +
        "if(p.offsetWidth>100||p.offsetHeight>100){p.style.display='none';break;}" +
        "p=p.parentElement;" +
        "}" +
        "}" +
        "}" +
        "}" +
        "hideAds(document);" +
        "new MutationObserver(function(ms){ms.forEach(function(m){" +
        "m.addedNodes.forEach(function(n){if(n.nodeType===1&&n.querySelectorAll)hideAds(n);});" +
        "})}).observe(document.documentElement,{childList:true,subtree:true});" +
        "})()",
        null);
    }
    if (Cfg.zoom) view.evaluateJavascript("(function(){var m=document.querySelector('meta[name=viewport]');if(m)m.content+=(m.content?',':'')+'user-scalable=yes,maximum-scale=5.0';else{var n=document.createElement('meta');n.name='viewport';n.content='width=device-width,initial-scale=1.0,user-scalable=yes,maximum-scale=5.0';document.head.appendChild(n);}})()", null);
    if (callback != null) callback.onPageFinished();
  }
}