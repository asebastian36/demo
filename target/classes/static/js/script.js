document.addEventListener('DOMContentLoaded', function() {
    // Manejar cambio de archivo
    const txtFile = document.getElementById('txtFile');
    if (txtFile) {
        txtFile.addEventListener('change', function(e) {
            const fileName = e.target.files[0] ? e.target.files[0].name : 'Seleccionar archivo .txt';
            const fileNameElement = document.getElementById('file-name');
            if (fileNameElement) {
                fileNameElement.textContent = fileName;
            }
        });
    }

    // Mostrar loading al enviar el formulario
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function() {
            const loadingElement = document.getElementById('loading');
            if (loadingElement) {
                loadingElement.style.display = 'block';
            }
        });
    }

    // Manejar la selección visual de opciones
    const optionButtons = document.querySelectorAll('.option-btn');
    if (optionButtons.length > 0) {
        optionButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                optionButtons.forEach(b => b.classList.remove('selected'));
                this.classList.add('selected');
                this.querySelector('input[type="radio"]').checked = true;

                // Mostrar/ocultar el campo de tamaño de n-grama
                toggleNgramSize();
            });
        });

        // Seleccionar la primera opción por defecto
        optionButtons[0].classList.add('selected');
    }

    // Mostrar/ocultar el campo de tamaño de n-grama
    function toggleNgramSize() {
        const ngramRadio = document.getElementById('ngramRadio');
        const ngramSizeContainer = document.getElementById('ngramSizeContainer');

        if (ngramRadio && ngramSizeContainer) {
            ngramSizeContainer.style.display = ngramRadio.checked ? 'block' : 'none';
        }
    }

    // Inicializar la visibilidad del campo n-grama
    toggleNgramSize();

    // Agregar event listeners para los radio buttons
    const unigramRadio = document.getElementById('unigramRadio');
    const ngramRadio = document.getElementById('ngramRadio');

    if (unigramRadio && ngramRadio) {
        unigramRadio.addEventListener('change', toggleNgramSize);
        ngramRadio.addEventListener('change', toggleNgramSize);
    }

    // Validar el formulario antes de enviar
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            const ngramRadio = document.getElementById('ngramRadio');

            if (ngramRadio && ngramRadio.checked) {
                const ngramSizeInput = document.getElementById('ngramSize');
                if (ngramSizeInput) {
                    const ngramSize = parseInt(ngramSizeInput.value);
                    if (ngramSize < 2) {
                        e.preventDefault();
                        alert('Por favor ingresa un tamaño de n-grama válido minimo mayor a 2');
                    }
                }
            }
        });
    }
});