document.addEventListener('DOMContentLoaded', function() {
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

    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function() {
            const loadingElement = document.getElementById('loading');
            if (loadingElement) {
                loadingElement.style.display = 'block';
            }
        });
    }

    const optionButtons = document.querySelectorAll('.option-btn');
    if (optionButtons.length > 0) {
        optionButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                optionButtons.forEach(b => b.classList.remove('selected'));
                this.classList.add('selected');
                this.querySelector('input[type="radio"]').checked = true;
                toggleNgramSize();
            });
        });
        optionButtons[0].classList.add('selected');
    }

    function toggleNgramSize() {
        const ngramRadio = document.getElementById('ngramRadio');
        const ngramSizeContainer = document.getElementById('ngramSizeContainer');
        if (ngramRadio && ngramSizeContainer) {
            ngramSizeContainer.style.display = ngramRadio.checked ? 'block' : 'none';
        }
    }

    toggleNgramSize();

    const unigramRadio = document.getElementById('unigramRadio');
    const ngramRadio = document.getElementById('ngramRadio');
    if (unigramRadio && ngramRadio) {
        unigramRadio.addEventListener('change', toggleNgramSize);
        ngramRadio.addEventListener('change', toggleNgramSize);
    }

    if (uploadForm) {
        uploadForm.addEventListener('submit', function(e) {
            const ngramRadio = document.getElementById('ngramRadio');
            if (ngramRadio && ngramRadio.checked) {
                const ngramSizeInput = document.getElementById('ngramSize');
                if (ngramSizeInput) {
                    const ngramSize = parseInt(ngramSizeInput.value);
                    if (ngramSize < 2) {
                        e.preventDefault();
                        alert('Por favor ingresa un tamaño de n-grama válido (mínimo 2).');
                    }
                }
            }
        });
    }
});