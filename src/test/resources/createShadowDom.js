(
  function () {
    var elemA = document.getElementById('shadow-root-a')
    if (typeof elemA.attachShadow == 'function') {
      elemA.attachShadow({mode: 'open'})
      var h1_a = document.createElement('h1')
      h1_a.textContent = 'Shadow DOM A'
      rootA.appendChild(h1_a)
      
      var rootB = document.getElementById('shadow-root-b').attachShadow({mode: 'open'})
      var h1_b = document.createElement('h1')
      h1_b.textContent = 'Shadow DOM B'
      rootB.appendChild(h1_b)
    }
  }
)()
