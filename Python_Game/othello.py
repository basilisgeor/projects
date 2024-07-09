#Basilis Georgakis A.M 3197

import random
import sys

turn = 1

def create_board():
    M = [[0 for _ in range(8)] for _ in range(8)]
    M[3][3] = 1
    M[3][4] = 2
    M[4][3] = 2
    M[4][4] = 1
    return M

def print_board(M):
    print('    0   1   2   3   4   5   6   7')
    print('   -------------------------------')
    for i in range(8):
        print(' ')
        print(i, end=' ')
        for j in range(8):
            print('| %d' % (M[i][j]), end=' ')
        print('|')
        print(' ')
        print('    -------------------------------')

def reverse_up(board, row, column, color):
    A = []
    for b in range(1, 8):
        if row - b >= 0 and board[row - b][column] == color:
            return A
        elif row - b >= 0 and board[row - b][column] != 0:
            A.append((row - b, column))
        else:
            return []
    return A

def reverse_down(board, row, column, color):
    C = []
    for d in range(1, 8):
        if row + d < 8 and board[row + d][column] == color:
            return C
        elif row + d < 8 and board[row + d][column] != 0:
            C.append((row + d, column))
        else:
            return []
    return C

def reverse_left(board, row, column, color):
    E = []
    for f in range(1, 8):
        if column - f >= 0 and board[row][column - f] == color:
            return E
        elif column - f >= 0 and board[row][column - f] != 0:
            E.append((row, column - f))
        else:
            return []
    return E

def reverse_right(board, row, column, color):
    G = []
    for h in range(1, 8):
        if column + h < 8 and board[row][column + h] == color:
            return G
        elif column + h < 8 and board[row][column + h] != 0:
            G.append((row, column + h))
        else:
            return []
    return G

def reverse_upright(board, row, column, color):
    B = []
    for a in range(1, 8):
        if row - a >= 0 and column + a < 8 and board[row - a][column + a] == color:
            return B
        elif row - a >= 0 and column + a < 8 and board[row - a][column + a] != 0:
            B.append((row - a, column + a))
        else:
            return []
    return B

def reverse_upleft(board, row, column, color):
    D = []
    for c in range(1, 8):
        if row - c >= 0 and column - c >= 0 and board[row - c][column - c] == color:
            return D
        elif row - c >= 0 and column - c >= 0 and board[row - c][column - c] != 0:
            D.append((row - c, column - c))
        else:
            return []
    return D

def reverse_downright(board, row, column, color):
    F = []
    for e in range(1, 8):
        if row + e < 8 and column + e < 8 and board[row + e][column + e] == color:
            return F
        elif row + e < 8 and column + e < 8 and board[row + e][column + e] != 0:
            F.append((row + e, column + e))
        else:
            return []
    return F

def reverse_downleft(board, row, column, color):
    H = []
    for g in range(1, 8):
        if row + g < 8 and column - g >= 0 and board[row + g][column - g] == color:
            return H
        elif row + g < 8 and column - g >= 0 and board[row + g][column - g] != 0:
            H.append((row + g, column - g))
        else:
            return []
    return H

def add_checkers(board, row, column, color):
    directions = [
        reverse_up(board, row, column, color),
        reverse_down(board, row, column, color),
        reverse_left(board, row, column, color),
        reverse_right(board, row, column, color),
        reverse_upright(board, row, column, color),
        reverse_upleft(board, row, column, color),
        reverse_downright(board, row, column, color),
        reverse_downleft(board, row, column, color)
    ]
    for direction in directions:
        for r, c in direction:
            board[r][c] = color

def human_play(board, color):
    while True:
        row = int(input('Enter row: '))
        column = int(input('Enter column: '))
        if 0 <= row < 8 and 0 <= column < 8 and board[row][column] == 0:
            add_checkers(board, row, column, color)
            board[row][column] = color
            break
        else:
            print("Invalid move, please try another move")

def computer_play(board, color):
    valid_moves = []
    for i in range(8):
        for j in range(8):
            if board[i][j] == 0:
                directions = [
                    reverse_up(board, i, j, color),
                    reverse_down(board, i, j, color),
                    reverse_left(board, i, j, color),
                    reverse_right(board, i, j, color),
                    reverse_upright(board, i, j, color),
                    reverse_upleft(board, i, j, color),
                    reverse_downright(board, i, j, color),
                    reverse_downleft(board, i, j, color)
                ]
                if any(directions):
                    valid_moves.append((i, j))
    if valid_moves:
        move = random.choice(valid_moves)
        add_checkers(board, move[0], move[1], color)
        board[move[0]][move[1]] = color

def print_score(board):
    black_count = sum(row.count(1) for row in board)
    white_count = sum(row.count(2) for row in board)
    print(f"Black: {black_count}, White: {white_count}")
    if black_count > white_count:
        print("The winner is the player with the black checkers")
    elif white_count > black_count:
        print("The winner is the player with the white checkers")
    else:
        print("The game is a tie")

def main():
    board = create_board()
    print_board(board)
    C = input("Do you want your opponent to be the Computer or another Player? ").strip().lower()
    if C == "computer":
        while True:
            human_play(board, 1)
            print_board(board)
            computer_play(board, 2)
            print_board(board)
            if not any(0 in row for row in board):
                break
    elif C == "player":
        while True:
            human_play(board, 1)
            print_board(board)
            human_play(board, 2)
            print_board(board)
            if not any(0 in row for row in board):
                break
    print_score(board)

if __name__ == "__main__":
    main()

   