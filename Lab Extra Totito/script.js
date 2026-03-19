const cells = document.querySelectorAll('.cell');
const statusText = document.querySelector('#status');
const resetBtn = document.querySelector('#resetBtn');

let currentPlayer = "X";
let board = ["", "", "", "", "", "", "", "", ""];
let gameActive = true;

const winningConditions = [
    [0, 1, 2], [3, 4, 5], [6, 7, 8], // Filas
    [0, 3, 6], [1, 4, 7], [2, 5, 8], // Columnas
    [0, 4, 8], [2, 4, 6]             // Diagonales
];

function handleCellClick(e) {
    const clickedCell = e.target;
    const cellIndex = parseInt(clickedCell.getAttribute('data-index'));

    if (board[cellIndex] !== "" || !gameActive) return;

    updateCell(clickedCell, cellIndex);
    checkResult();
}

function updateCell(cell, index) {
    board[index] = currentPlayer;
    cell.innerText = currentPlayer;
    cell.classList.add('taken');
}

function checkResult() {
    let roundWon = false;

    for (let condition of winningConditions) {
        let a = board[condition[0]];
        let b = board[condition[1]];
        let c = board[condition[2]];
        if (a === "" || b === "" || c === "") continue;
        if (a === b && b === c) {
            roundWon = true;
            break;
        }
    }

    if (roundWon) {
        statusText.innerText = `¡Victoria para ${currentPlayer}!`;
        statusText.classList.replace('alert-primary', 'alert-success');
        gameActive = false;
        return;
    }

    if (!board.includes("")) {
        statusText.innerText = "¡Empate!";
        statusText.classList.replace('alert-primary', 'alert-warning');
        gameActive = false;
        return;
    }

    currentPlayer = currentPlayer === "X" ? "O" : "X";
    statusText.innerText = `Turno de: ${currentPlayer}`;
}

function resetGame() {
    currentPlayer = "X";
    board = ["", "", "", "", "", "", "", "", ""];
    gameActive = true;
    statusText.innerText = "Turno de: X";
    statusText.className = "alert alert-primary h4";
    cells.forEach(cell => {
        cell.innerText = "";
        cell.classList.remove('taken');
    });
}

cells.forEach(cell => cell.addEventListener('click', handleCellClick));
resetBtn.addEventListener('click', resetGame);