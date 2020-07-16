// Update the client ID and search app ID for your deployment.
let searchConfig = {
  "clientId" : "1040072754811-q4me3b44r3u68a7kait3vu02b0q55dsh.apps.googleusercontent.com",
  "searchAppId": "searchapplications/e96ae9836b91bbe7954835ee35a098f3"
};

let resultsContainer;

function onLoad() {
  gapi.load('client:auth2:cloudsearch-widget', initializeApp);
}

function ResultsContainerAdapter() {
  this.selectedSource = null;
}

ResultsContainerAdapter.prototype.decorateFacetResultElement = function(element, result) {
    for(var i=1; i<element.childNodes.length; ++i) {
        element.childNodes[i].childNodes[1].style.color="#000000";
        if(element.childNodes[i].childNodes.length == 3) {
          element.childNodes[i].childNodes[1].textContent += "         ("+result.buckets[i-1].count+")";
          element.childNodes[i].removeChild(element.childNodes[i].childNodes[2]);
        }
    }
    var text = element.childNodes[0].childNodes[0].textContent;
    element.childNodes[0].childNodes[0].setAttribute("style", "color:#440066;");
    switch(text) {
        case "authorname" : element.childNodes[0].childNodes[0].textContent = "Author Names";
        break;
        case "disease" : element.childNodes[0].childNodes[0].textContent = "Diseases";
        break;
        case "org" : element.childNodes[0].childNodes[0].textContent = "Organizations";
        break;
        case "symptom" : element.childNodes[0].childNodes[0].textContent = "Symptoms";
        break;
        case "virus" : element.childNodes[0].childNodes[0].textContent = "Virus";
        break;
        case "pathogen" : element.childNodes[0].childNodes[0].textContent = "Pathogens";
        break;
        case "drug" : element.childNodes[0].childNodes[0].textContent = "Drugs";
        break;
        default:
        break;
    }
    console.log(element.parentNode);
    var nd = document.getElementById("facet_results");
    console.log(nd);
    console.log(nd.childNodes.length);
}

ResultsContainerAdapter.prototype.decorateSearchResultElement = function(element, result) {
element.setAttribute("style", "border-radius: 25px; margin: 25px 0px 25px 0px;");
element.childNodes[1].childNodes[0].style.color  = "#440066";
}

async function initializeApp() {
  await gapi.auth2.init({
      'clientId': searchConfig.clientId,
      'scope': 'https://www.googleapis.com/auth/cloud_search.query'
  });

  let auth = gapi.auth2.getAuthInstance();

  let onSignInChanged = (isSignedIn) => {
    document.getElementById("app").hidden = !isSignedIn;
    document.getElementById("welcome").hidden = isSignedIn;
    document.getElementById("questions").hidden = isSignedIn;
    if (resultsContainer) {
      resultsContainer.clear();
    }
  }

  auth.isSignedIn.listen(onSignInChanged);
  onSignInChanged(auth.isSignedIn.get());

  document.getElementById("sign-in").onclick = (e) =>  auth.signIn();
  document.getElementById("sign-out").onclick = (e) => auth.signOut();
   var resultsContainerAdapter = new ResultsContainerAdapter();

  gapi.config.update('cloudsearch.config/apiVersion', 'v1');
  resultsContainer = new gapi.cloudsearch.widget.resultscontainer.Builder()
    .setSearchApplicationId(searchConfig.searchAppId)
    .setAdapter(resultsContainerAdapter)
    .setSearchResultsContainerElement(document.getElementById('search_results'))
    .setFacetResultsContainerElement(document.getElementById('facet_results'))
    .build();

  const searchBox = new gapi.cloudsearch.widget.searchbox.Builder()
    .setSearchApplicationId(searchConfig.searchAppId)
    .setInput(document.getElementById('search_input'))
    .setAnchor(document.getElementById('suggestions_anchor'))
    .setResultsContainer(resultsContainer)
    .build();
}


