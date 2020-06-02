(
  function () {
    var elemA = document.getElementById('shadow-root-a')
    var elemB = document.getElementById('shadow-root-b')
    
    if (typeof elemA.attachShadow == 'function') {
      var rootA = elemA.attachShadow({mode: 'open'})
      var h1_a = document.createElement('h1')
      h1_a.textContent = 'Shadow DOM A'
      rootA.appendChild(h1_a)
      
      var rootB = elemB.attachShadow({mode: 'open'})
      var h1_b = document.createElement('h1')
      h1_b.textContent = 'Shadow DOM B'
      rootB.appendChild(h1_b)
    }
  }
)()
